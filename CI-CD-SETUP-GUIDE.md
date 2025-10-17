# Guide de Configuration CI/CD - SmartDish

## Vue d'ensemble

Cette CI/CD est conçue pour le projet **SmartDish** (RecipeYouLove) - une plateforme de recommandation de recettes basée sur l'IA et l'apprentissage par renforcement.

## Architecture

### Environnements

- **Integration** : Branche `develop` → `https://soa-<microservice>-int.smartdish.app`
- **Production** : Branche `main` → `https://soa-<microservice>.smartdish.app`

**Exemples d'URLs :**
- `https://soa-recipe-recommender-int.smartdish.app` (Integration)
- `https://soa-recipe-recommender.smartdish.app` (Production)
- `https://soa-user-service-int.smartdish.app` (Integration)
- `https://soa-ingredient-parser.smartdish.app` (Production)

### Déclencheurs

La pipeline se déclenche automatiquement sur :
- Push sur `main` → Build + Deploy Production
- Push sur `develop` → Build + Deploy Integration  
- Push sur `feat/**` → Build uniquement (pas de déploiement)
- Push sur `fix/**` → Build uniquement (pas de déploiement)
- Pull Request vers `main` → Build + SonarQube (obligatoire)

## 🚀 Démarrage rapide

**Si vous partez de zéro**, suivez le guide complet : **[SETUP_COMPLET.md](./SETUP_COMPLET.md)**

Ce guide contient :
- Installation du cluster Kubernetes (K3s local ou Cloud)
- Configuration DNS avec Cloudflare
- Installation automatique de toute l'infrastructure
- Scripts prêts à l'emploi
- Troubleshooting complet

## Structure du projet

```
RecipeYouLove/
├── .github/
│   ├── workflows/                    # Workflows CI/CD
│   │   ├── pipeline-orchestrator.yml # Orchestrateur principal
│   │   ├── config-vars.yml           # Détection auto config
│   │   ├── build-maven.yml           # Build & Tests
│   │   ├── build-docker-image.yml    # Build Docker
│   │   ├── check-conformity-image.yml # Security scan
│   │   ├── sonar-analysis.yml        # SonarQube
│   │   └── deploy-kubernetes.yml     # Déploiement K8s
│   └── helm/
│       └── chart/                    # Helm chart réutilisable
│           ├── Chart.yaml
│           ├── values.yaml
│           └── k8s-manifests/        # Manifests Kubernetes
│               ├── deployment.yaml   # Avec Vault Agent Injector
│               ├── service.yaml
│               ├── ingress.yaml      # SSL automatique
│               ├── configmap.yaml
│               ├── serviceaccount.yaml
│               ├── hpa.yaml
│               └── _helpers.tpl
├── src/                              # Code Java
├── pom.xml                           # Maven (nom du MS détecté ici)
├── Dockerfile
├── SETUP_COMPLET.md                  # Guide complet de setup
└── CI-CD-SETUP-GUIDE.md             # Ce fichier
```

## Configuration requise

### 1. Variables GitHub

Dans **Settings → Secrets and variables → Actions → Variables** :

```
COVERAGE_THRESHOLD = 60
BASE_DOMAIN = smartdish.app
```

### 2. Secrets GitHub

Dans **Settings → Secrets and variables → Actions → Secrets** :

**Kubernetes** :
```
KUBECONFIG = <votre kubeconfig encodé en base64>
```

Pour obtenir votre KUBECONFIG :
```bash
cat ~/.kube/config | base64 -w 0
```

**SonarQube** (optionnel si vous n'utilisez pas SonarQube) :
```
SONAR_TOKEN = <votre token>
SONAR_HOST_URL = https://sonarqube.smartdish.app
```

### 3. Environments GitHub

Dans **Settings → Environments**, créez :
- `integration` (pas de protection)
- `production` (avec reviewers si souhaité)

## Infrastructure Kubernetes

### Ce qui est inclus dans les Helm Charts

**Deployment** (`.github/helm/chart/k8s-manifests/deployment.yaml`) :
- Support Vault Agent Injector pour secrets automatiques
- Health checks (liveness & readiness probes)
- Security context (runAsNonRoot)
- Rolling update strategy

**Ingress** (`.github/helm/chart/k8s-manifests/ingress.yaml`) :
- Exposition HTTPS avec SSL automatique (Let's Encrypt)
- Redirection HTTP → HTTPS
- Support NGINX Ingress Controller

**Service** (`.github/helm/chart/k8s-manifests/service.yaml`) :
- ClusterIP pour exposition interne

**ConfigMap** (`.github/helm/chart/k8s-manifests/configmap.yaml`) :
- Configuration de l'application (JAVA_OPTS, profiles, etc.)

**HPA** (`.github/helm/chart/k8s-manifests/hpa.yaml`) :
- Autoscaling horizontal (désactivé par défaut)

### Ressources allouées automatiquement

**Integration** :
- Replicas: 2
- CPU: 250m (request) / 500m (limit)
- Memory: 256Mi (request) / 512Mi (limit)

**Production** :
- Replicas: 3
- CPU: 500m (request) / 1000m (limit)
- Memory: 512Mi (request) / 1Gi (limit)

## Gestion des secrets avec Vault

Les secrets sont injectés automatiquement via Vault Agent Injector.

**Structure dans Vault** :
```
secret/data/integration/<nom-microservice>/db/
  - DB_URL
  - DB_USERNAME
  - DB_PASSWORD

secret/data/integration/<nom-microservice>/mongodb/
  - MONGODB_URI
  - MONGODB_DATABASE
```

**Exemple pour le microservice "recipe-recommander"** :
```bash
vault kv put secret/integration/recipe-recommander/db \
  DB_URL="jdbc:postgresql://postgres:5432/recipes" \
  DB_USERNAME="recipe_user" \
  DB_PASSWORD="password123"

vault kv put secret/integration/recipe-recommander/mongodb \
  MONGODB_URI="mongodb://mongo:27017" \
  MONGODB_DATABASE="recipes"
```

## Détails de la pipeline

### 1. Configuration automatique
- Détecte la branche (main, develop, feat/*, fix/*)
- Extrait le nom du microservice depuis `pom.xml` (artifactId)
- Détermine l'environnement et si déploiement nécessaire
- Calcule l'URL finale

### 2. Build & Tests
- Compile avec Maven
- Exécute les tests unitaires
- Génère le rapport JaCoCo
- **Vérifie la couverture >= 60%** (bloque si insuffisant)
- Package le JAR

### 3. SonarQube (uniquement PR vers main)
- Analyse la qualité du code
- Vérifie le Quality Gate
- Bloque le merge si échec

### 4. Build Docker Image
- Build l'image avec le JAR
- Tag : `<branche>-<commit-sha>`
- Push vers GitHub Container Registry (ghcr.io)

### 5. Security Scan
- Scan avec Trivy
- Upload vers GitHub Security
- Rapport détaillé généré

### 6. Déploiement Kubernetes
- Création namespace avec labels
- Secrets Docker registry
- Génération values.yaml personnalisé
- Déploiement Helm
- Vérification rollout
- Attente certificat SSL
- Test health check

## Flux de travail complet

### Développer une nouvelle fonctionnalité

```bash
# 1. Créer une branche feature
git checkout -b feat/add-recipe-rating

# 2. Développer
# ... code ...

# 3. Commit et push
git add .
git commit -m "feat: add recipe rating system"
git push origin feat/add-recipe-rating

# 4. GitHub Actions exécute:
#    ✓ Build Maven
#    ✓ Tests
#    ✓ Couverture >= 60%
#    ✓ Build Docker
#    ✓ Security Scan
#    ✗ PAS de déploiement

# 5. Merger vers develop
git checkout develop
git merge feat/add-recipe-rating
git push origin develop

# 6. GitHub Actions déploie automatiquement:
#    → https://soa-univ-soa-int.smartdish.app

# 7. Créer PR vers main
# 8. SonarQube s'exécute (obligatoire)
# 9. Merger vers main
# 10. Déploiement automatique en Production:
#     → https://soa-univ-soa.smartdish.app
```

## URLs des microservices

Le nom est extrait automatiquement du `pom.xml` :

```xml
<artifactId>recipe-recommander</artifactId>
```

**URLs générées** :
- Integration : `https://soa-recipe-recommander-int.smartdish.app`
- Production : `https://soa-recipe-recommander.smartdish.app`

Tous les microservices SmartDish suivront ce pattern :
- `soa-ingredient-parser-int.smartdish.app`
- `soa-user-preference-service.smartdish.app`
- `soa-ml-training-service-int.smartdish.app`
- etc.

## Adaptation pour les microservices forkés

1. **Forkez** ce repository
2. **Modifiez** le `pom.xml` (artifactId = nom de votre microservice)
3. **Créez les secrets Vault** pour votre microservice (voir SETUP_COMPLET.md)
4. **Push** vers develop → Déploiement automatique !

La CI/CD détecte tout automatiquement depuis le `pom.xml`.

## Commandes utiles

### Vérifier le déploiement

```bash
# Pods
kubectl get pods -n integration -l app=<nom-ms>

# Logs
kubectl logs -n integration -l app=<nom-ms> -f

# Ingress
kubectl get ingress -n integration

# Certificat SSL
kubectl get certificate -n integration
```

### Tester l'application

```bash
# Health check
curl https://soa-<nom-ms>-int.smartdish.app/actuator/health

# Info
curl https://soa-<nom-ms>-int.smartdish.app/actuator/info
```

### Accéder à Vault

```bash
kubectl port-forward -n vault svc/vault 8200:8200
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root

# Lister les secrets
vault kv list secret/integration/

# Voir un secret
vault kv get secret/integration/<nom-ms>/db
```

## Troubleshooting

Consultez le fichier **SETUP_COMPLET.md** section "PARTIE 8 : Troubleshooting" pour :
- Problèmes de certificat SSL
- Pods qui ne démarrent pas
- Images Docker introuvables
- Erreurs de déploiement

## Support

Pour toute question :
1. Consultez **SETUP_COMPLET.md** pour le guide complet
2. Vérifiez les logs dans GitHub Actions
3. Vérifiez les pods Kubernetes : `kubectl describe pod -n integration <pod-name>`
