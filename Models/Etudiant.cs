namespace Models;

using Npgsql;
using Models.Cnx;

public class Etudiant
{
	string id;
	string nom;
	string idpromotion;
	string idgroupe;

	public string Id {
        get { return id; }
        set { id = value; }
    }
	public string Nom {
        get { return nom; }
        set { nom = value; }
    }
	public string Idpromotion {
        get { return idpromotion; }
        set { idpromotion = value; }
    }
	public string Idgroupe {
        get { return idgroupe; }
        set { idgroupe = value; }
    }

	public Etudiant() { }


    public int Save(NpgsqlConnection cnx) {
        int affectedRow = 0;
    
        string sql = "INSERT INTO etudiant (id,nom,idpromotion,idgroupe) VALUES(@id,@nom,@idpromotion,@idgroupe)";
    
        this.id = Connex.createId(cnx, "etudiant_sequence", "ETU", 7);
        using(NpgsqlCommand command = new NpgsqlCommand(sql, cnx))
        {
            Console.WriteLine("New id: " + this.id);
            command.Parameters.AddWithValue("@id", this.id);
            command.Parameters.AddWithValue("@nom", this.nom);
            command.Parameters.AddWithValue("@idpromotion", this.idpromotion);
            command.Parameters.AddWithValue("@idgroupe", this.idgroupe);
    
            affectedRow += command.ExecuteNonQuery();
        }
    
        return affectedRow;
    }
    
    
	public Etudiant? FindById(NpgsqlConnection cnx, Object id) {
        Etudiant? etudiant = null;
    
        string sql = "SELECT * FROM etudiant WHERE id = @id";
    
        using(NpgsqlCommand command = new NpgsqlCommand(sql,cnx)){
            command.Parameters.AddWithValue("@id", id);
    
            using(NpgsqlDataReader reader = command.ExecuteReader()){
                if(reader.HasRows){
                    while(reader.Read()){
                        string idDB = reader.GetString(0);
                        string nomDB = reader.GetString(1);
                        string idpromotionDB = reader.GetString(2);
                        string idgroupeDB = reader.GetString(3);
    
                        etudiant = new Etudiant();
                        etudiant.id = idDB;
                        etudiant.nom = nomDB;
                        etudiant.idpromotion = idpromotionDB;
                        etudiant.idgroupe = idgroupeDB;
                    }
                }
            }
        }
    
        return etudiant;
    }
    
    
	public List<Etudiant> FindAll(NpgsqlConnection cnx) {
        List<Etudiant> etudiantList = new List<Etudiant>();
    
        string sql = "SELECT * FROM etudiant";
    
        using(NpgsqlCommand command = new NpgsqlCommand(sql,cnx)){
            using(NpgsqlDataReader reader = command.ExecuteReader()){
                if(reader.HasRows){
                    Etudiant? etudiantTemp = null;
                    while(reader.Read()){
                        string idDB = reader.GetString(0);
                        string nomDB = reader.GetString(1);
                        string idpromotionDB = reader.GetString(2);
                        string idgroupeDB = reader.GetString(3);
    
                        etudiantTemp = new Etudiant();
                        etudiantTemp.id = idDB;
                        etudiantTemp.nom = nomDB;
                        etudiantTemp.idpromotion = idpromotionDB;
                        etudiantTemp.idgroupe = idgroupeDB;
    
                        etudiantList.Add(etudiantTemp);
                    }
                }
            }
        }
    
        return etudiantList;
    }
    
    
	public List<Etudiant> FindAllPg(NpgsqlConnection cnx, Object debut) {
        List<Etudiant> etudiantList = new List<Etudiant>();
    
        string sql = "SELECT * FROM etudiant OFFSET @debut LIMIT 5";
    
        using(NpgsqlCommand command = new NpgsqlCommand(sql,cnx)){
            command.Parameters.AddWithValue("@debut", debut);
    
            using(NpgsqlDataReader reader = command.ExecuteReader()){
                if(reader.HasRows){
                    Etudiant? etudiantTemp = null;
                    while(reader.Read()){
                        string idDB = reader.GetString(0);
                        string nomDB = reader.GetString(1);
                        string idpromotionDB = reader.GetString(2);
                        string idgroupeDB = reader.GetString(3);
    
                        etudiantTemp = new Etudiant();
                        etudiantTemp.id = idDB;
                        etudiantTemp.nom = nomDB;
                        etudiantTemp.idpromotion = idpromotionDB;
                        etudiantTemp.idgroupe = idgroupeDB;
    
                        etudiantList.Add(etudiantTemp);
                    }
                }
            }
        }
    
        return etudiantList;
    }
    
    
	public int Update(NpgsqlConnection cnx, Object id) {
        int updatedRow = 0;
    
        string sql = "UPDATE etudiant SET nom=@nom,idpromotion=@idpromotion,idgroupe=@idgroupe WHERE id = @id";
    
        using(NpgsqlCommand command = new NpgsqlCommand(sql, cnx))
        {
            command.Parameters.AddWithValue("@nom", this.nom);
            command.Parameters.AddWithValue("@idpromotion", this.idpromotion);
            command.Parameters.AddWithValue("@idgroupe", this.idgroupe);
    
            command.Parameters.AddWithValue("@id", id);
    
            updatedRow += command.ExecuteNonQuery();
        }
    
        return updatedRow;
    }
    
    
	public int Delete(NpgsqlConnection cnx, Object id) {
        int deletedRow = 0;
    
        string sql = "DELETE FROM etudiant WHERE id = @id";
    
        using(NpgsqlCommand command = new NpgsqlCommand(sql, cnx))
        {
            command.Parameters.AddWithValue("@id", id);
    
            deletedRow += command.ExecuteNonQuery();
        }
    
        return deletedRow;
    }
}
