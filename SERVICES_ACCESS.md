# 🚀 RecipeYouLove Template - Guide d'accès aux services

## 📋 À propos de ce template

Ce template fournit l'infrastructure complète pour développer les microservices de l'application **RecipeYouLove**. Tous les services de base (bases de données, interfaces d'administration, stockage) sont préconfigurés et prêts à l'emploi avec **Docker Compose** pour une standardisation complète.

## 🔐 Configuration sécurisée

**⚠️ IMPORTANT :** Les identifiants d'accès ne sont **PAS documentés ici** pour des raisons de sécurité. Ils sont configurés dans le fichier `.env` fourni par l'administrateur du projet.

## 🔗 Services disponibles

### 🗄️ Base de données MySQL
- **Host** : `localhost:3307`
- **Base de données** : `recipe_db` *(à adapter selon votre microservice)*
- **Chaîne de connexion** : `jdbc:mysql://localhost:3307/recipe_db`
- **Identifiants** : *Configurés dans le fichier .env*

### 🌐 PhpMyAdmin (Interface MySQL)
- **URL** : [http://localhost:8080](http://localhost:8080)
- **Usage** : Administration et visualisation des données MySQL
- **Identifiants** : *Configurés dans le fichier .env*

### 🍃 Base de données MongoDB
- **Host** : `localhost:27017`
- **Base de données** : `recipe_mongodb` *(à adapter selon votre microservice)*
- **Chaîne de connexion** : `mongodb://[user]:[password]@localhost:27017/recipe_mongodb?authSource=admin`
- **Identifiants** : *Configurés dans le fichier .env*

### 🌿 Mongo Express (Interface MongoDB)
- **URL** : [http://localhost:8081](http://localhost:8081)
- **Usage** : Administration et visualisation des données MongoDB
- **Identifiants** : *Configurés dans le fichier .env*

### 🪣 MinIO (Stockage S3-compatible)
- **Console** : [http://localhost:9001](http://localhost:9001)
- **API** : [http://localhost:9000](http://localhost:9000)
- **Usage** : Stockage de fichiers compatible S3
- **Identifiants** : *Configurés dans le fichier .env*
- **Buckets préconfigurés** :
  - `artifacts-bucket` - Artefacts de build
  - `datasets-bucket` - Jeux de données
  - `reports-bucket` - Rapports générés
  - `backups-bucket` - Sauvegardes

### 🖥️ Application Spring Boot (Template)
- **URL** : [http://localhost:8090](http://localhost:8090) *(une fois démarrée)*
- **Health Check** : [http://localhost:8090/health](http://localhost:8090/health)
- **Actuator** : [http://localhost:8090/actuator](http://localhost:8090/actuator)
- **Test DB** : [http://localhost:8090/api/database/test](http://localhost:8090/api/database/test)

---

## 🐳 Docker Compose - Standardisation

### Pourquoi Docker Compose ?

Docker Compose garantit que **tous les développeurs** travaillent avec :
- ✅ Les mêmes versions de bases de données
- ✅ Les mêmes ports et configurations
- ✅ Le même environnement isolé
- ✅ Les mêmes services préconfigurés

### Commandes Docker Compose

```bash
# Démarrer tous les services (standardisé)
docker-compose up -d

# Arrêter tous les services
docker-compose down

# Voir l'état des services
docker-compose ps

# Redémarrer un service spécifique
docker-compose restart [service_name]

# Voir les logs en temps réel
docker-compose logs -f [service_name]

# Voir les logs de tous les services
docker-compose logs -f
```

---

## 🛠️ Gestion de l'application Spring Boot

```bash
# Compiler le projet
mvn clean compile

# Installer les dépendances
mvn clean install

# Démarrer l'application
mvn spring-boot:run

# Démarrer en mode debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

---

## 🔧 Configuration pour nouveau microservice

### 1. Récupération du fichier .env

```bash
# 1. Recevoir le fichier .env de l'administrateur
# 2. Le placer à la racine du projet
# 3. Ne jamais le commiter (protégé par .gitignore)
```

### 2. Adapter les ports (fichier `.env`)

Si vous développez plusieurs microservices en parallèle :

```env
# Exemple pour ms-authentification
SERVER_PORT=8091
MYSQL_PORT=3308
MONGO_PORT=27018
PHPMYADMIN_PORT=8081
MONGO_EXPRESS_PORT=8082
```

### 3. Adapter les noms de bases de données

```env
# Exemple pour ms-recette
MYSQL_DATABASE=ms_recette_db
MONGO_DATABASE=ms_recette_mongodb
```

### 4. Tester la configuration

Après modification, redémarrez les services :

```bash
docker-compose down
docker-compose up -d
mvn spring-boot:run
```

---

## 🩺 Diagnostic et résolution de problèmes

### Vérifier l'état des services

```bash
# Status général
docker-compose ps

# Logs détaillés
docker-compose logs mysql
docker-compose logs mongodb
docker-compose logs minio
```

### Tests de connectivité

```bash
# Test MySQL (sans exposer les credentials)
docker exec mysql-local mysql --execute="SELECT 'MySQL OK' as status;"

# Test MongoDB
docker exec mongodb-local mongosh --eval "db.adminCommand('ping')"

# Test application Spring Boot
curl http://localhost:8090/health
curl http://localhost:8090/api/database/test
```

### Problèmes courants

| Problème | Solution |
|----------|----------|
| **Port déjà utilisé** | Modifier les ports dans le fichier `.env` |
| **Services pas "Healthy"** | Attendre 1-2 minutes ou redémarrer |
| **Connexion DB échoue** | Vérifier le fichier `.env` ou contacter l'administrateur |
| **Application ne démarre pas** | Vérifier les logs avec `mvn spring-boot:run` |
| **Fichier .env manquant** | Contacter l'administrateur pour recevoir le fichier |

---

## ✅ Checklist de validation

### Pour le template de base :

- [ ] Fichier `.env` reçu et placé à la racine
- [ ] Tous les services Docker sont "Healthy"
- [ ] PhpMyAdmin accessible sur port 8080
- [ ] Mongo Express accessible sur port 8081
- [ ] MinIO Console accessible sur port 9001
- [ ] Application Spring Boot démarre sans erreur
- [ ] Test de connectivité DB réussit (`/api/database/test`)

### Pour un nouveau microservice :

- [ ] Fichier `.env` adapté avec ports personnalisés
- [ ] Noms de bases de données adaptés
- [ ] Package Java renommé
- [ ] `pom.xml` mis à jour avec le bon nom
- [ ] Application démarre sur le bon port
- [ ] Tests de connectivité passent

---

## 🚀 Pipeline CI/CD (En cours de développement)

### Prochainement disponible :
- **Kubernetes** - Déploiement en conteneurs orchestrés
- **Vault** - Gestion sécurisée des secrets et credentials
- **Pipeline automatisée** - Tests, build et déploiement automatiques

*Ces fonctionnalités avancées sont en cours de développement par l'équipe infrastructure.*

---

## 📞 Support et ressources

### En cas de problème
1. Vérifier que Docker Desktop est démarré
2. Consulter les logs : `docker-compose logs`
3. Redémarrer les services : `docker-compose down && docker-compose up -d`
4. **Contacter l'administrateur** pour les problèmes de configuration .env

### Documentation complémentaire
- [README principal](README.md) - Guide complet du template
- [Guide de rebase avec le template](GUIDE_REBASE_TEMPLATE.md) - Maintenir le microservice à jour
- [Configuration Docker](docker-compose.yml)

---

*🎯 Ce template avec Docker Compose garantit un environnement de développement standardisé et sécurisé pour tous les contributeurs.*
