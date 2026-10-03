# Migrations de la base de données

Flyway est le seul mécanisme de création et d'évolution du schéma. Au démarrage,
il applique les migrations manquantes avant la validation des entités par Hibernate
(`ddl-auto: validate`). Hibernate ne crée, ne modifie et ne supprime plus les tables.
L'initialisation automatique par `schema.sql` et `data.sql` est désactivée.

## Première initialisation

Cette transition suppose une base vide, sans données à conserver. Ne pas activer
`baseline-on-migrate` pour contourner une ancienne base non vide : son schéma
pourrait être incompatible avec la migration initiale.

1. Arrêter le backend avant toute remise à zéro. Une ancienne instance utilisant
   `create-drop` peut supprimer les tables à son arrêt.
2. Si l'ancienne base contient encore des tables, vérifier la connexion et le nom
   de la base locale dédiée à JobStream. Dans cette base uniquement, exécuter
   manuellement `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` avec le
   propriétaire de la base. **Cette opération supprime toutes les données du
   schéma public.** Ne jamais l'utiliser sur une base partagée ou de production.
3. Démarrer le backend avec les variables de connexion habituelles. Flyway
   applique `V1__initial_schema.sql` et crée `flyway_schema_history`.
4. Créer un compte via l'application : aucun compte de démonstration n'est chargé.

Le volume PostgreSQL du Docker Compose conserve les données. Ne pas le supprimer
et ne pas utiliser `docker compose down -v` pour un arrêt ordinaire.

## Faire évoluer le schéma

Ajouter un fichier dans `api/src/main/resources/db/migration`, par exemple
`V2__add_job_status.sql`. Le numéro doit être unique et croissant ; le séparateur
entre la version et le nom est un double underscore.

Une migration déjà appliquée ne doit plus être modifiée ni renommée. Flyway
enregistre les versions et leurs sommes de contrôle. Chaque évolution ultérieure
est un nouveau script, contenant les `ALTER TABLE`, créations d'index ou
transformations de données nécessaires. Aligner les entités avec le nouveau
schéma dans la même tranche.

Avant un déploiement, sauvegarder la base et tester les migrations sur une copie
représentative. Une correction se fait par une nouvelle migration ; ne pas
supprimer l'historique et ne pas utiliser `repair` pour masquer une modification
de script. Flyway n'apporte pas de retour arrière automatique aux migrations SQL
versionnées.

## Vérification

Les tests PostgreSQL Testcontainers utilisent les mêmes migrations et la même
validation Hibernate que l'application. Les fixtures sous
`api/src/test/resources/fixtures` sont chargées explicitement dans les schémas
isolés des tests et ne sont pas incluses dans l'application livrée.

Exécuter `cd api && sh mvnw verify`. Pour vérifier manuellement la persistance,
créer un compte, sauvegarder une offre, arrêter puis redémarrer le backend et
retrouver les mêmes données après reconnexion.

Référence : [initialisation et Flyway dans Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html).
