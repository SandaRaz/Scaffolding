Definir le package par defaut pour les Models:
#		set default package $package$

Definir le package par defaut pour les Controllers:
#		set default controller package $package$

Definir la table par defaut pour l'authentification:
#		set auth table $table$
	
Commandes des generations des CRUDs:
1. Pour generer les CRUDs de la TABLE D'AUTHENTIFICATION par defaut sans securité (CUD sans authentification):
#  		generate $tableName$ crud for $backend$ with $frontend$ auth column $usermail$ and $key$**

2. Pour generer les CRUDs de la TABLE D'AUTHENTIFICATION par defaut avec securité (CUD apres authentification)
#		generate $tableName$ crud for $backend$ with $frontend$ securized auth column $usermail$ and $key$

3. Pour generer les CRUDs d'une table sans securité (CUD sans authentification):
#		generate $tableName$ crud for $backend$ with $frontend$

4. Pour generer les CRUDs d'une table avec securité (CUD apres authentification):
# 		generate $tableName$ crud for $backend$ with $frontend$ securized


** Back-end dispo: csharp
** Front-end dispo: angular