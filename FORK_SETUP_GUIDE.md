# 🔧 Guide de Configuration après Fork

Ce dépôt est un **template parent** pour créer des microservices indépendants. Après avoir forké ce dépôt pour créer un nouveau microservice, suivez ces étapes pour adapter la CI/CD.

## 📋 Étapes de Configuration

### 1. **Adapter les workflows CI/CD**

Les workflows sont configurés pour une architecture multi-microservices. Après le fork, vous devez les adapter pour un **projet monolithique** (un seul microservice par dépôt).

#### Fichiers à modifier :

- `.github/workflows/build-maven.yml`
- `.github/workflows/build-docker-image.yml`
- `.github/workflows/check-coverage.yml`
- `.github/workflows/check-conformity-image.yml`
- `.github/workflows/deploy-Kubernetes.yml`
- `.github/workflows/sonar-analysis.yml`

#### Modifications à effectuer :

**Avant (template avec matrice) :**
```yaml
jobs:
  build:
    name: 📦 Build ${{ matrix.microservice }}
    strategy:
      matrix:
        microservice:
          - ms_utilisateur
          - ms-recette
          - ms-feedback
    steps:
      - name: 📦 Compile
        working-directory: microservices/${{ matrix.microservice }}
        run: mvn compile
```

**Après (microservice forké) :**
```yaml
jobs:
  build:
    name: 📦 Build Application
    runs-on: ubuntu-latest
    steps:
      - name: 📦 Compile
        run: mvn compile
```

### 2. **Personnaliser les variables**

#### Dans `config-vars.yml`, mettre à jour :
```yaml
# Remplacer le nom générique par le nom de votre microservice
-Dsonar.projectKey=<NOM_MICROSERVICE>
-Dsonar.projectName="<NOM_MICROSERVICE>"
```

#### Dans `build-docker-image.yml`, adapter :
```yaml
# Remplacer
images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/${{ matrix.microservice }}

# Par
images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
```

#### Dans `deploy-Kubernetes.yml`, adapter :
```yaml
# Remplacer la référence au chart Helm
working-directory: helm-charts/microservice-template

# Par
working-directory: helm-charts/<nom-microservice>
```

### 3. **Supprimer les références aux microservices**

Dans tous les workflows, **supprimer** :
- La section `strategy.matrix.microservice`
- Les `working-directory: microservices/${{ matrix.microservice }}`
- Les noms d'artefacts avec `${{ matrix.microservice }}`

### 4. **Mettre à jour les noms d'artefacts**

**Avant :**
```yaml
- name: 📤 Upload JAR
  uses: actions/upload-artifact@v3
  with:
    name: ${{ matrix.microservice }}-jar
    path: microservices/${{ matrix.microservice }}/target/*.jar
```

**Après :**
```yaml
- name: 📤 Upload JAR
  uses: actions/upload-artifact@v3
  with:
    name: application-jar
    path: target/*.jar
```

### 5. **Configurer les secrets GitHub**

Ajoutez les secrets suivants dans `Settings > Secrets and variables > Actions` :

#### Secrets requis :
- `SONAR_TOKEN` : Token SonarQube
- `SONAR_HOST_URL` : URL du serveur SonarQube
- `KUBE_CONFIG` : Configuration Kubernetes (base64)
- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`
- `MONGO_HOST`, `MONGO_PORT`, `MONGO_USER`, `MONGO_PASSWORD`, `MONGO_DATABASE`
- `CODECOV_TOKEN` (optionnel)

### 6. **Adapter le fichier .env**

Mettez à jour `.env` avec les valeurs spécifiques à votre microservice :

```bash
# Application
SERVER_PORT=8091  # Port unique pour chaque microservice

# Nom de la base de données spécifique
MYSQL_DATABASE=ms_utilisateur_db
MONGO_DATABASE=ms_utilisateur_mongodb
```

### 7. **Mettre à jour application.properties**

Dans `src/main/resources/application.properties` :

```properties
spring.application.name=ms-utilisateur  # Nom de votre microservice
server.port=${SERVER_PORT}
```

### 8. **Adapter le pom.xml**

Mettez à jour les informations du projet :

```xml
<groupId>com.recipeyoulove</groupId>
<artifactId>ms-utilisateur</artifactId>
<version>1.0.0</version>
<name>ms-utilisateur</name>
<description>Microservice de gestion des utilisateurs</description>
```

### 9. **Créer/Adapter le Dockerfile**

Créez un `Dockerfile` à la racine si nécessaire :

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 10. **Adapter les charts Helm**

Dans `helm-charts/`, créez un dossier spécifique pour votre microservice :

```bash
helm-charts/
  ms-utilisateur/
    Chart.yaml
    values.yaml
    values-dev.yaml
    values-staging.yaml
    values-production.yaml
    templates/
```

## 🔄 Script de Migration Automatique

Exécutez ce script après le fork pour automatiser les modifications :

```bash
#!/bin/bash

# Nom du microservice (à personnaliser)
MICROSERVICE_NAME="ms-utilisateur"
MICROSERVICE_PORT="8091"

echo "🔧 Configuration du microservice: $MICROSERVICE_NAME"

# 1. Supprimer les références aux matrices dans les workflows
find .github/workflows -name "*.yml" -type f -exec sed -i '/strategy:/,/microservice:/d' {} \;
find .github/workflows -name "*.yml" -type f -exec sed -i "s|working-directory: microservices/\${{ matrix.microservice }}||g" {} \;
find .github/workflows -name "*.yml" -type f -exec sed -i "s|microservices/\${{ matrix.microservice }}/||g" {} \;
find .github/workflows -name "*.yml" -type f -exec sed -i "s|\${{ matrix.microservice }}|$MICROSERVICE_NAME|g" {} \;

# 2. Mettre à jour application.properties
sed -i "s|spring.application.name=.*|spring.application.name=$MICROSERVICE_NAME|g" src/main/resources/application.properties

# 3. Mettre à jour .env
sed -i "s|SERVER_PORT=.*|SERVER_PORT=$MICROSERVICE_PORT|g" .env

echo "✅ Configuration terminée!"
echo "⚠️  N'oubliez pas de configurer les secrets GitHub!"
```

## 📝 Checklist Finale

Avant de pousser votre code :

- [ ] Workflows adaptés (pas de matrice)
- [ ] Noms des artefacts mis à jour
- [ ] `.env` personnalisé
- [ ] `application.properties` mis à jour
- [ ] `pom.xml` personnalisé
- [ ] Secrets GitHub configurés
- [ ] Charts Helm créés/adaptés
- [ ] README.md mis à jour avec le nom du microservice
- [ ] Tests CI/CD validés sur une branche de test

## 🆘 Aide

En cas de problème, consultez :
- La documentation GitHub Actions
- Les logs de CI/CD dans l'onglet "Actions"
- Le README principal du projet

