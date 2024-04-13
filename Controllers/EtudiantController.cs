namespace Angular3.Controllers;

using Microsoft.AspNetCore.Cors;
using Microsoft.AspNetCore.Mvc;
using Npgsql;
using Models.Cnx;
using Models;

[ApiController]
[Route("[controller]")]
public class EtudiantController : ControllerBase
{
    private readonly Etudiant _etudiant;
	private readonly ILogger<EtudiantController> _logger;

	public EtudiantController(ILogger<EtudiantController> logger)
	{
	    _logger = logger;
        _etudiant = new Etudiant();
	}

    // GET: /Etudiant
	[HttpGet]
	public List<Etudiant> Get()
	{
	    NpgsqlConnection cnx = Connex.getConnection();
	    cnx.Open();
	    List<Etudiant> etudiantList = _etudiant.FindAll(cnx);
	    cnx.Close();

	    return etudiantList;
	}

	[HttpGet("pagination")]
    public List<Etudiant> GetPg(int debut)
    {
        NpgsqlConnection cnx = Connex.getConnection();
        cnx.Open();
        List<Etudiant> etudiantList = _etudiant.FindAllPg(cnx, debut);
        cnx.Close();

        return etudiantList;
    }

    // GET: /Etudiant/id
	[HttpGet("{id}")]
    public Etudiant Get(string id)
    {
        NpgsqlConnection cnx = Connex.getConnection();
        cnx.Open();
        Etudiant etudiant = _etudiant.FindById(cnx, id);
        cnx.Close();

        return etudiant;
    }

    // POST: /Etudiant
    [HttpPost]
    public IActionResult Post([FromBody] Etudiant etudiant){
        try
        {
            NpgsqlConnection cnx = Connex.getConnection();
            cnx.Open();
            etudiant.Save(cnx);
            cnx.Close();

            return Ok(new { message = "Etudiant enregistré avec succès" });
        }
        catch(Exception e)
        {
            return StatusCode(500, $"Une erreur s'est produite: {e.Message}");
        }
    }

    // PUT: /Etudiant/id
    [HttpPut("{id}")]
    public IActionResult Put(string id, [FromBody] Etudiant etudiant){
        NpgsqlConnection cnx = Connex.getConnection();
        cnx.Open();
        etudiant.Update(cnx, id);
        cnx.Close();

        return NoContent();
    }

    // DELETE: /Etudiant/id
    [HttpDelete("{id}")]
    public IActionResult Delete(string id){
        NpgsqlConnection cnx = Connex.getConnection();
        cnx.Open();
        _etudiant.Delete(cnx, id);
        cnx.Close();

        return NoContent();
    }
}
