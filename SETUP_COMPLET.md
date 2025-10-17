# SmartDish - Guide de Setup Complet (Démarrage de Zéro)

Ce guide vous accompagne pas à pas pour déployer l'infrastructure complète SmartDish depuis zéro.

## 📋 Vue d'ensemble

**Domaines utilisés :**
- **Integration** : `https://soa-<microservice>-int.smartdish.app`
- **Production** : `https://soa-<microservice>.smartdish.app`

**Architecture :**
```
GitHub Actions → Build/Test → Docker Image → Kubernetes (K3s/K8s)
                                                ↓
                                         Vault (secrets)
                                                ↓
                                         Ingress NGINX → Cert-Manager (SSL)
                                                ↓
                                    https://soa-recipe-service.smartdish.app
```

---

## 🚀 PARTIE 1 : Setup du Cluster Kubernetes (Local ou Cloud)

### Option A : Cluster Local avec K3s (Recommandé pour débuter)

```bash
# 1. Installer K3s (Linux/WSL)
curl -sfL https://get.k3s.io | sh -

# 2. Configurer kubectl
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config

# 3. Vérifier l'installation
kubectl get nodes
```

### Option B : Cluster Cloud (GKE, EKS, AKS)

**Google Kubernetes Engine (GKE) :**
```bash
# Créer un cluster
gcloud container clusters create smartdish-cluster \
  --zone europe-west1-b \
  --num-nodes 3 \
  --machine-type n1-standard-2

# Récupérer les credentials
gcloud container clusters get-credentials smartdish-cluster --zone europe-west1-b
```

---

## 🌐 PARTIE 2 : Configuration du DNS

### Avec Cloudflare (Recommandé)

1. **Acheter un domaine** (ex: `smartdish.app`) sur Namecheap, Google Domains, etc.

2. **Configurer Cloudflare** :
   - Ajouter votre domaine sur Cloudflare
   - Changer les nameservers chez votre registrar

3. **Récupérer l'IP externe du LoadBalancer** :
```bash
# Attendre que K3s/K8s démarre
kubectl get svc -A

# Pour K3s, l'IP sera celle de votre machine
# Pour Cloud, attendre l'IP du LoadBalancer (après installation de NGINX Ingress)
```

4. **Créer les enregistrements DNS sur Cloudflare** :
```
Type: A
Name: *.smartdish.app
Value: <VOTRE-IP-PUBLIQUE>
Proxy: Désactivé (nuage gris)
TTL: Auto

Type: A
Name: *.int.smartdish.app
Value: <VOTRE-IP-PUBLIQUE>
Proxy: Désactivé (nuage gris)
TTL: Auto
```

### Sans domaine (Test local avec nip.io)

Pour tester sans acheter de domaine :
```bash
# Utilisez nip.io qui fournit des DNS automatiques
# Par exemple, si votre IP est 192.168.1.100 :
# Les URLs seront : soa-recipe-service.192.168.1.100.nip.io
```

---

## 📦 PARTIE 3 : Installation de l'Infrastructure Kubernetes

### Script d'installation automatique

Créez le fichier `setup-k8s-infrastructure.sh` :

```bash
#!/bin/bash

set -e

echo "=== Installation de l'infrastructure SmartDish ==="
echo ""

# 1. Installation de NGINX Ingress Controller
echo "1. Installation de NGINX Ingress Controller..."
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer \
  --set controller.metrics.enabled=true \
  --wait

echo "✅ NGINX Ingress Controller installé"
echo ""

# 2. Récupérer l'IP du LoadBalancer
echo "2. Récupération de l'IP du LoadBalancer..."
EXTERNAL_IP=""
while [ -z "$EXTERNAL_IP" ]; do
  echo "Attente de l'IP externe..."
  EXTERNAL_IP=$(kubectl get svc ingress-nginx-controller -n ingress-nginx -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
  [ -z "$EXTERNAL_IP" ] && sleep 10
done

echo "✅ IP du LoadBalancer: $EXTERNAL_IP"
echo ""
echo "⚠️  IMPORTANT: Configurez votre DNS pour pointer *.smartdish.app vers $EXTERNAL_IP"
echo "   Attendez que le DNS soit propagé avant de continuer..."
read -p "Appuyez sur Entrée quand le DNS est configuré..."

# 3. Installation de Cert-Manager
echo "3. Installation de Cert-Manager..."
helm repo add jetstack https://charts.jetstack.io
helm repo update

helm upgrade --install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true \
  --wait

echo "✅ Cert-Manager installé"
echo ""

# 4. Créer le ClusterIssuer Let's Encrypt
echo "4. Configuration de Let's Encrypt..."
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@smartdish.app
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
---
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-staging
spec:
  acme:
    server: https://acme-staging-v02.api.letsencrypt.org/directory
    email: admin@smartdish.app
    privateKeySecretRef:
      name: letsencrypt-staging
    solvers:
    - http01:
        ingress:
          class: nginx
EOF

echo "✅ ClusterIssuer Let's Encrypt créé"
echo ""

# 5. Installation de HashiCorp Vault
echo "5. Installation de HashiCorp Vault..."
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

helm upgrade --install vault hashicorp/vault \
  --namespace vault \
  --create-namespace \
  --set "server.dev.enabled=true" \
  --set "server.dev.devRootToken=root" \
  --set "injector.enabled=true" \
  --set "ui.enabled=true" \
  --wait

echo "✅ Vault installé en mode DEV"
echo ""
echo "⚠️  MODE DEV: Le root token est 'root' - À CHANGER EN PRODUCTION!"
echo ""

# 6. Créer les namespaces
echo "6. Création des namespaces..."
kubectl create namespace integration --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace production --dry-run=client -o yaml | kubectl apply -f -
kubectl label namespace integration environment=integration --overwrite
kubectl label namespace production environment=production --overwrite

echo "✅ Namespaces créés"
echo ""

echo "=== Installation terminée! ==="
echo ""
echo "Prochaines étapes:"
echo "1. Configurer Vault: ./setup-vault.sh"
echo "2. Configurer GitHub Secrets"
echo "3. Push du code pour déclencher la CI/CD"
```

**Exécuter le script :**
```bash
chmod +x setup-k8s-infrastructure.sh
./setup-k8s-infrastructure.sh
```

---

## 🔐 PARTIE 4 : Configuration de Vault

### Script de configuration Vault

Créez le fichier `setup-vault.sh` :

```bash
#!/bin/bash

set -e

echo "=== Configuration de HashiCorp Vault pour SmartDish ==="
echo ""

# Variables
VAULT_ADDR="http://localhost:8200"
VAULT_TOKEN="root"  # Token du mode DEV

# Port-forward Vault (à exécuter dans un autre terminal)
echo "Démarrage du port-forward vers Vault..."
kubectl port-forward -n vault svc/vault 8200:8200 &
PORTFORWARD_PID=$!
sleep 5

export VAULT_ADDR
export VAULT_TOKEN

# 1. Activer le moteur de secrets KV v2
echo "1. Activation du moteur de secrets KV v2..."
vault secrets enable -path=secret kv-v2 || echo "Déjà activé"

# 2. Créer une policy pour les microservices
echo "2. Création de la policy microservices..."
vault policy write microservice-policy - <<EOF
path "secret/data/integration/*" {
  capabilities = ["read", "list"]
}
path "secret/data/production/*" {
  capabilities = ["read", "list"]
}
EOF

# 3. Activer l'authentification Kubernetes
echo "3. Configuration de l'authentification Kubernetes..."
vault auth enable kubernetes || echo "Déjà activé"

# Récupérer les infos du cluster
K8S_HOST=$(kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[0].cluster.server}')
K8S_CA_CERT=$(kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[0].cluster.certificate-authority-data}' | base64 -d)

# Créer un ServiceAccount pour Vault
kubectl create sa vault-auth -n vault --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: vault-auth-secret
  namespace: vault
  annotations:
    kubernetes.io/service-account.name: vault-auth
type: kubernetes.io/service-account-token
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: vault-auth
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:auth-delegator
subjects:
- kind: ServiceAccount
  name: vault-auth
  namespace: vault
EOF

sleep 5

# Récupérer le token du ServiceAccount
SA_TOKEN=$(kubectl get secret vault-auth-secret -n vault -o jsonpath='{.data.token}' | base64 -d)

# Configurer Vault pour Kubernetes
vault write auth/kubernetes/config \
  kubernetes_host="$K8S_HOST" \
  kubernetes_ca_cert="$K8S_CA_CERT" \
  token_reviewer_jwt="$SA_TOKEN"

# 4. Créer les roles pour chaque environnement
echo "4. Création des roles Kubernetes..."
vault write auth/kubernetes/role/integration \
  bound_service_account_names='*' \
  bound_service_account_namespaces=integration \
  policies=microservice-policy \
  ttl=1h

vault write auth/kubernetes/role/production \
  bound_service_account_names='*' \
  bound_service_account_namespaces=production \
  policies=microservice-policy \
  ttl=1h

# 5. Créer des secrets d'exemple pour le premier microservice
echo "5. Création des secrets d'exemple..."

# Secrets pour Integration
vault kv put secret/integration/univ-soa/db \
  DB_URL="jdbc:postgresql://postgres.integration.svc.cluster.local:5432/smartdish" \
  DB_USERNAME="smartdish_user" \
  DB_PASSWORD="ChangeMe_Integration123!"

vault kv put secret/integration/univ-soa/mongodb \
  MONGODB_URI="mongodb://mongo.integration.svc.cluster.local:27017" \
  MONGODB_DATABASE="smartdish_integration"

# Secrets pour Production
vault kv put secret/production/univ-soa/db \
  DB_URL="jdbc:postgresql://postgres.production.svc.cluster.local:5432/smartdish" \
  DB_USERNAME="smartdish_user" \
  DB_PASSWORD="SuperSecurePassword_Prod456!"

vault kv put secret/production/univ-soa/mongodb \
  MONGODB_URI="mongodb://mongo.production.svc.cluster.local:27017" \
  MONGODB_DATABASE="smartdish_production"

echo ""
echo "✅ Vault configuré avec succès!"
echo ""
echo "Secrets créés pour le microservice 'univ-soa':"
echo "- secret/integration/univ-soa/db"
echo "- secret/integration/univ-soa/mongodb"
echo "- secret/production/univ-soa/db"
echo "- secret/production/univ-soa/mongodb"
echo ""
echo "Pour ajouter un nouveau microservice, utilisez:"
echo "  vault kv put secret/integration/<nom-ms>/db DB_URL=... DB_USERNAME=... DB_PASSWORD=..."
echo "  vault kv put secret/integration/<nom-ms>/mongodb MONGODB_URI=... MONGODB_DATABASE=..."

# Arrêter le port-forward
kill $PORTFORWARD_PID

echo ""
echo "=== Configuration Vault terminée! ==="
```

**Exécuter le script :**
```bash
chmod +x setup-vault.sh
./setup-vault.sh
```

---

## ⚙️ PARTIE 5 : Configuration de GitHub

### 1. Créer les Variables GitHub

Dans votre repository GitHub : **Settings** → **Secrets and variables** → **Actions** → **Variables**

Créez :
```
COVERAGE_THRESHOLD = 60
BASE_DOMAIN = smartdish.app
```

### 2. Créer les Secrets GitHub

Dans **Settings** → **Secrets and variables** → **Actions** → **Secrets**

Créez :

**KUBECONFIG** :
```bash
# Encoder votre kubeconfig en base64
cat ~/.kube/config | base64 -w 0
```
Copiez le résultat dans le secret `KUBECONFIG`

**SONAR_TOKEN** et **SONAR_HOST_URL** :
```
SONAR_TOKEN = <votre token SonarQube>
SONAR_HOST_URL = https://sonarqube.smartdish.app
```

*Note: Si vous n'avez pas SonarQube, vous pouvez utiliser SonarCloud (gratuit pour projets publics) : https://sonarcloud.io*

### 3. Créer les Environments GitHub

Dans **Settings** → **Environments**, créez :

1. **integration**
   - Pas de protection requise

2. **production**
   - ✅ Required reviewers : vous-même
   - ✅ Wait timer : 0 minutes (ou plus si vous voulez un délai)

---

## 🗄️ PARTIE 6 : Déployer les bases de données (Optionnel)

Si vos microservices ont besoin de PostgreSQL et MongoDB :

```bash
# PostgreSQL pour Integration
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres-integration bitnami/postgresql \
  --namespace integration \
  --set auth.username=smartdish_user \
  --set auth.password=ChangeMe_Integration123! \
  --set auth.database=smartdish

# MongoDB pour Integration
helm install mongo-integration bitnami/mongodb \
  --namespace integration \
  --set auth.enabled=false

# PostgreSQL pour Production
helm install postgres-production bitnami/postgresql \
  --namespace production \
  --set auth.username=smartdish_user \
  --set auth.password=SuperSecurePassword_Prod456! \
  --set auth.database=smartdish \
  --set replication.enabled=true \
  --set replication.readReplicas=2

# MongoDB pour Production
helm install mongo-production bitnami/mongodb \
  --namespace production \
  --set architecture=replicaset \
  --set replicaCount=3
```

---

## 🚀 PARTIE 7 : Premier déploiement

### 1. Vérifier la structure du projet

Votre projet doit avoir :
```
RecipeYouLove/
├── .github/
│   ├── workflows/         # ✅ Workflows CI/CD
│   └── helm/
│       └── chart/         # ✅ Helm chart
│           └── k8s-manifests/  # ✅ Manifests Kubernetes
├── src/                   # ✅ Code Java
├── pom.xml               # ✅ Maven config
└── Dockerfile            # ✅ Dockerfile
```

### 2. Push vers develop (déploiement Integration)

```bash
git checkout develop
git add .
git commit -m "feat: setup complete infrastructure"
git push origin develop
```

**Résultat attendu :**
- ✅ La pipeline GitHub Actions démarre
- ✅ Build Maven + Tests
- ✅ Couverture de code vérifiée (>60%)
- ✅ Build Docker Image
- ✅ Security Scan
- ✅ Déploiement sur Integration
- ✅ Application accessible sur `https://soa-univ-soa-int.smartdish.app`

### 3. Vérifier le déploiement

```bash
# Voir les pods
kubectl get pods -n integration

# Voir les logs
kubectl logs -n integration -l app=univ-soa

# Voir l'ingress
kubectl get ingress -n integration

# Tester l'application
curl https://soa-univ-soa-int.smartdish.app/actuator/health
```

### 4. Push vers main (déploiement Production)

```bash
git checkout main
git merge develop
git push origin main
```

**Résultat attendu :**
- ✅ Application déployée sur `https://soa-univ-soa.smartdish.app`

---

## 🔧 PARTIE 8 : Troubleshooting

### Problème : Le certificat SSL ne s'émet pas

```bash
# Vérifier cert-manager
kubectl get certificate -n integration

# Voir les logs cert-manager
kubectl logs -n cert-manager -l app=cert-manager

# Solution : Vérifier que le DNS pointe bien vers l'IP du LoadBalancer
nslookup soa-univ-soa-int.smartdish.app
```

### Problème : Le pod ne démarre pas

```bash
# Voir les events
kubectl describe pod -n integration <pod-name>

# Voir les logs
kubectl logs -n integration <pod-name>

# Solution courante : vérifier que les secrets Vault existent
kubectl port-forward -n vault svc/vault 8200:8200
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root
vault kv get secret/integration/univ-soa/db
```

### Problème : L'image Docker n'est pas trouvée

```bash
# Vérifier que le secret ghcr existe
kubectl get secret ghcr-secret -n integration

# Recréer le secret
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=<votre-username> \
  --docker-password=<votre-PAT> \
  --namespace=integration
```

---

## 📊 PARTIE 9 : Ajouter un nouveau microservice

Quand vous forkez pour créer un nouveau microservice :

1. **Créer les secrets dans Vault** :
```bash
kubectl port-forward -n vault svc/vault 8200:8200 &
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root

# Remplacez 'recipe-service' par le nom de votre microservice
vault kv put secret/integration/recipe-service/db \
  DB_URL="jdbc:postgresql://postgres.integration:5432/recipes" \
  DB_USERNAME="recipe_user" \
  DB_PASSWORD="password123"

vault kv put secret/integration/recipe-service/mongodb \
  MONGODB_URI="mongodb://mongo.integration:27017" \
  MONGODB_DATABASE="recipes"
```

2. **Push vers develop** : La CI/CD détectera automatiquement le nom depuis `pom.xml`

3. **Profit!** L'application sera accessible sur `https://soa-recipe-service-int.smartdish.app`

---

## 🎉 Félicitations !

Vous avez maintenant une infrastructure SmartDish complète avec :
- ✅ Kubernetes cluster configuré
- ✅ NGINX Ingress avec SSL automatique
- ✅ HashiCorp Vault pour les secrets
- ✅ CI/CD complète avec GitHub Actions
- ✅ Déploiements automatiques sur Integration et Production

**URLs de vos microservices :**
- Integration : `https://soa-<nom-ms>-int.smartdish.app`
- Production : `https://soa-<nom-ms>.smartdish.app`

Bon développement ! 🚀

