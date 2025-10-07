# 🍳 SmartDish - Template Microservices

## 📖 Vue d'ensemble

Ce projet constitue le **template parent** pour tous les microservices de l'application **SmartDish**. Il s'agit d'un générateur de recettes intelligent qui recommande des plats à l'utilisateur en fonction des ingrédients saisis et de ses retours. Le système intègre un agent d'apprentissage par renforcement (RL) qui ajuste ses recommandations au fil du temps.

## 🏗️ Architecture Microservices

Cette application sera composée de plusieurs microservices, chacun ayant sa propre responsabilité :

- **🔐 ms-authentification** - Gestion de l'authentification et des utilisateurs
- **🥗 ms-recette** - Gestion des recettes et des ingrédients
- **🤖 ms-feedback** - Gestion des retours utilisateurs et moteur de recommandation avec IA/RL

## 🎯 Objectif du Template

Ce template fournit :
- ✅ Configuration complète de l'environnement de développement avec **Docker Compose**
- ✅ Connexions aux bases de données (MySQL + MongoDB)
- ✅ Stockage MinIO pour les fichiers
- ✅ Interfaces d'administration web
- ✅ Configuration sécurisée avec variables d'environnement
- ✅ Structure de projet Spring Boot standardisée

## 🐳 Docker Compose - Standardisation

**Docker Compose est utilisé pour :**
- ✅ **Exécution locale standardisée** - Tous les développeurs auront le même environnement
- ✅ **Configuration unifiée** - Même version des bases de données, même ports, mêmes services
- ✅ **Isolation des services** - Chaque microservice a ses propres ressources
- ✅ **Déploiement simplifié** - Un seul `docker-compose up -d` pour tout démarrer

Cela garantit que l'environnement de développement soit **identique chez tous les contributeurs**.

## 🔐 Gestion du fichier .env

**⚠️ IMPORTANT - Configuration sécurisée :**

Le fichier `.env` contenant les configurations sensibles (mots de passe, clés) **ne sera PAS inclus dans le repository** pour des raisons de sécurité.

**Processus de récupération du .env :**
1. Le fichier `.env` sera **fourni individuellement par l'administrateur projet**
2. Distribution via **message privé** ou **canal sécurisé du groupe projet**
3. **Placer le fichier `.env` reçu à la racine du projet**
4. Ne jamais commiter ce fichier (déjà protégé par .gitignore)

```bash
# Structure attendue :
votre-projet/
├── .env                 # ← Fichier reçu de l'administrateur
├── docker-compose.yml
└── ...
```

## 🔄 Maintenir votre microservice à jour

### Importance du rebase du template

Le template parent est régulièrement mis à jour avec :
- ✅ **Nouvelles fonctionnalités** - Améliorations de l'infrastructure
- ✅ **Corrections de sécurité** - Mise à jour des dépendances et configurations
- ✅ **Optimisations** - Performance et bonnes pratiques
- ✅ **Nouvelles versions** - Spring Boot, bases de données, Docker

**⚠️ Il est ESSENTIEL de maintenir votre microservice synchronisé avec le template.**

### Comment faire le rebase du template

#### 1. Configuration initiale (à faire une seule fois)

```bash
# Ajouter le template comme remote "upstream"
git remote add upstream https://github.com/votre-org/SmartDish.git

# Vérifier les remotes configurés
git remote -v
# origin    https://github.com/votre-username/ms-authentification.git (fetch)
# origin    https://github.com/votre-username/ms-authentification.git (push)
# upstream  https://github.com/votre-org/SmartDish.git (fetch)
# upstream  https://github.com/votre-org/SmartDish.git (push)
```

#### 2. Processus de mise à jour (à répéter régulièrement)

```bash
# 1. S'assurer d'être sur la branche principale
git checkout main

# 2. Sauvegarder vos modifications locales
git stash

# 3. Récupérer les dernières modifications du template
git fetch upstream

# 4. Rebaser votre microservice sur le template mis à jour
git rebase upstream/main

# 5. Résoudre les conflits s'il y en a (voir section ci-dessous)

# 6. Restaurer vos modifications locales
git stash pop

# 7. Pousser les modifications
git push origin main --force-with-lease
```

#### 3. Résolution des conflits de rebase

En cas de conflits, Git vous indiquera les fichiers concernés :

```bash
# Voir les fichiers en conflit
git status

# Éditer manuellement chaque fichier en conflit
# Garder vos adaptations spécifiques (noms, ports, etc.)
# Intégrer les nouvelles fonctionnalités du template

# Marquer les conflits comme résolus
git add fichier-resolu.java
git add autre-fichier-resolu.properties

# Continuer le rebase
git rebase --continue
```

#### 4. Vérification après rebase

```bash
# Vérifier que tout compile
mvn clean compile

# Tester l'infrastructure
docker-compose down
docker-compose up -d

# Tester l'application
mvn spring-boot:run
```

### Conflits courants et résolutions

| Type de conflit | Action recommandée |
|-----------------|-------------------|
| **pom.xml** | Garder votre `artifactId` et `name`, intégrer nouvelles dépendances |
| **application.properties** | Garder votre `spring.application.name`, intégrer nouvelles configs |
| **Package Java** | Garder votre package, adapter les nouveaux imports si nécessaire |
| **docker-compose.yml** | Garder vos ports personnalisés, intégrer nouveaux services |

### Planning de mise à jour recommandé

- 🔄 **Hebdomadaire** - Vérifier s'il y a des mises à jour du template
- 📅 **Avant chaque release** - Obligatoire avant de déployer en production
- 🚨 **Immédiatement** - En cas d'alerte de sécurité du template

### Commandes utiles pour le suivi

```bash
# Voir les commits du template non intégrés
git log --oneline HEAD..upstream/main

# Voir les différences avec le template
git diff upstream/main

# Voir l'historique des rebases
git reflog
```

### En cas de problème lors du rebase

```bash
# Annuler le rebase en cours
git rebase --abort

# Revenir à l'état avant le rebase
git reset --hard HEAD@{1}

# Demander de l'aide avec les logs
git log --oneline --graph -10
```

## 🚀 Démarrage rapide

### Prérequis
- Java 21+
- Maven 3.6+
- Docker & Docker Compose
- Git

### Installation

1. **Cloner le template** (ou forker pour créer un nouveau microservice)
```bash
git clone https://github.com/votre-org/SmartDish.git
cd SmartDish
```

2. **Récupérer le fichier .env**
```bash
# Attendre de recevoir le fichier .env de l'administrateur
# Le placer à la racine du projet
```

3. **Démarrer l'infrastructure avec Docker Compose**
```bash
docker-compose up -d
```

4. **Vérifier que tous les services sont en ligne**
```bash
docker-compose ps
# Attendre que tous les services soient "Healthy"
```

5. **Compiler et démarrer l'application**
```bash
mvn clean install
mvn spring-boot:run
```

## 🔗 Accès aux services

Une fois tous les services démarrés :

| Service | URL | Accès |
|---------|-----|-------|
| **Application Spring Boot** | http://localhost:8090 | Direct |
| **PhpMyAdmin (MySQL)** | http://localhost:8080 | Interface d'administration |
| **Mongo Express (MongoDB)** | http://localhost:8081 | Interface d'administration |
| **MinIO Console** | http://localhost:9001 | Interface d'administration |

*Les identifiants d'accès sont configurés dans le fichier .env fourni par l'administrateur.*

## 🔧 Adapter le template pour un nouveau microservice

### 1. Configuration du projet

**a) Modifier le `pom.xml`**
```xml
<groupId>com.smartdish</groupId>
<artifactId>ms-nom-de-votre-microservice</artifactId>
<name>ms-nom-de-votre-microservice</name>
<description>Description de votre microservice</description>
```

**b) Renommer le package principal**
```bash
# Déplacer de :
src/main/java/com/springbootTemplate/univ/soa/
# Vers :
src/main/java/com/smartdish/[nom-microservice]/
```

**c) Mettre à jour le fichier principal `Application.java`**
```java
package com.smartdish.[nom-microservice];

@SpringBootApplication
public class [NomMicroservice]Application {
    public static void main(String[] args) {
        SpringApplication.run([NomMicroservice]Application.class, args);
    }
}
```

### 2. Configuration des bases de données

**a) Modifier le fichier `.env` (reçu de l'administrateur)**
```env
# Adapter selon votre microservice
MYSQL_DATABASE=nom_microservice_db
MONGO_DATABASE=nom_microservice_mongodb

# Changer les ports si nécessaire pour éviter les conflits
SERVER_PORT=8091  # ou autre port libre
MYSQL_PORT=3308   # si vous avez plusieurs microservices
MONGO_PORT=27018  # si vous avez plusieurs microservices
```

**b) Mettre à jour `application.properties`**
```properties
spring.application.name=nom-de-votre-microservice
```

### 3. Structure recommandée pour chaque microservice

```
src/main/java/com/smartdish/[microservice]/
├── Application.java
├── config/
│   ├── DatabaseConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── [Entity]Controller.java
│   └── HealthController.java
├── service/
│   ├── [Entity]Service.java
│   └── [Entity]ServiceImpl.java
├── repository/
│   ├── [Entity]Repository.java
│   └── [Entity]MongoRepository.java
├── model/
│   ├── entity/
│   │   └── [Entity].java
│   └── dto/
│       ├── [Entity]RequestDto.java
│       └── [Entity]ResponseDto.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── [Custom]Exception.java
```

## 🗃️ Configuration des bases de données par microservice

### Recommandations par microservice :

| Microservice | Base principale | Base secondaire | Justification |
|--------------|----------------|-----------------|---------------|
| **ms-authentification** | MySQL | - | Données relationnelles critiques |
| **ms-recette** | MySQL | MongoDB | Recettes structurées + métadonnées flexibles |
| **ms-feedback** | MongoDB | - | Données non-structurées, ML/IA |

## 🐳 Configuration Docker pour développement

### Ports par défaut recommandés :

| Microservice | Port App | Port MySQL | Port MongoDB |
|--------------|----------|------------|--------------|
| **ms-authentification** | 8091 | 3308 | 27018 |
| **ms-recette** | 8092 | 3309 | 27019 |
| **ms-feedback** | 8093 | 3310 | 27020 |

### Commandes Docker pour chaque microservice

```bash
# Arrêter le template
docker-compose down

# Modifier le .env avec les nouveaux ports
# Redémarrer avec la nouvelle configuration
docker-compose up -d
```

## 🚀 Pipeline CI/CD (En développement)

**🔄 Prochainement disponible :**
- Pipeline CI/CD complète
- Intégration Kubernetes pour le déploiement
- Gestion des secrets avec Vault
- Déploiement automatisé en environnements de test/production

*Cette fonctionnalité est actuellement en cours de développement par l'équipe infrastructure.*

## 🔒 Sécurité et bonnes pratiques

### Variables d'environnement
- ✅ Toujours utiliser le fichier `.env` fourni par l'administrateur
- ✅ Ne jamais commiter le fichier `.env` (déjà dans .gitignore)
- ✅ Signaler tout problème de configuration à l'administrateur

### Base de données
- ✅ Créer des utilisateurs spécifiques pour chaque microservice
- ✅ Utiliser des bases de données séparées
- ✅ Implémenter des migrations avec Flyway/Liquibase
- ✅ Configurer les backup automatiques

## 📚 Documentation détaillée

- [Guide d'accès aux services](SERVICES_ACCESS.md)
- [Guide de rebase avec le template](GUIDE_REBASE_TEMPLATE.md)
- [Configuration Docker](docker-compose.yml)

## 🤝 Contribution

1. Forker ce template pour créer un nouveau microservice
2. Recevoir le fichier `.env` de l'administrateur
3. Suivre les conventions de nommage
4. Mettre à jour la documentation
5. Tester localement avec Docker Compose
6. Créer une Pull Request avec une description détaillée

## 📞 Support

Pour toute question sur ce template ou l'architecture microservices :
- Créer une issue sur ce repository
- Consulter la documentation dans `/docs`
- Contacter l'administrateur pour les questions de configuration
- Signaler les problèmes d'environnement Docker

---

🎯 **Ce template est conçu pour accélérer le développement des microservices SmartDish tout en garantissant une cohérence architecturale et une sécurité optimale.**
