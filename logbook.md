# Journal de bord

## Explication du projet 


Idée de POC : "Liste de Pokémon + Détail"
Utiliser l’API publique GraphQL de PokéAPI (GraphQL wrapper).

Une liste (Pokémon) → un détail (Pokémon individuel).

Possibilité de démontrer une navigation A → B avec un discriminant (id ou name).

API GraphQL publique et gratuite.

## Structure du POC
Écran A – Liste des Pokémon
Affiche une liste des Pokémon avec leur nom.

Lorsqu’on clique sur un Pokémon, on pré-fetch un discriminant minimal (e.g. name ou id) pour valider l’existence.

Navigue vers l’écran B avec juste ce discriminant.

Écran B – Détail du Pokémon
Fetch les infos détaillées à partir du discriminant.

Affiche un squelette de chargement (loading state).

Affiche les données une fois chargées.

## Setup initial

- Creation d'un EmptyProject
- Init de Apollo
	+ Note: J'ai pas trop compris le setup initial de schema.graphqls
	


## TL;DR lecture androids best practices



- **Single source of truth ** (SSOT) in app, with **Unidirectionnal Data Flow (UDF)**  drived by **StateFlow**

(via stateflow): Datasource ------> UI

(via event):     Datasource <----- UI

- Use repository, which is the only one to deal with data source (and not viewmodel). Its here where you deal with local vs remote DS
- 
