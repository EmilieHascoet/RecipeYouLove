# 🔄 Rebase avec le template parent

## Quand faire le rebase ?
- **Hebdomadaire** - Vérifier les mises à jour
- **Avant release** - Obligatoire
- **Alerte sécurité** - Immédiat

## Configuration (une fois)
```bash
git remote add upstream https://github.com/votre-org/RecipeYouLove.git
```

## Processus de rebase
```bash
git stash
git fetch upstream
git rebase upstream/main
# Résoudre conflits si nécessaire
git stash pop
git push origin main --force-with-lease
```

## Conflits courants
- **pom.xml** : Garder votre `artifactId`, intégrer nouvelles dépendances
- **application.properties** : Garder votre nom d'app, intégrer nouvelles configs

## Dépannage
```bash
git rebase --abort  # Annuler
git reset --hard HEAD@{1}  # Revenir en arrière
```
