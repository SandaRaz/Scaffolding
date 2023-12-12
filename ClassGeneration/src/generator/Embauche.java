package model; 
 
import java.util.Date; 
 
public class Embauche {
	int id; 
	String idemp; 
	Date dateembauche; 
	double salaire; 
    public int getId(){ 
        return this.id; 
    } 
    public void setId(int id){ 
        this.id = id; 
    } 
 
    public String getIdemp(){ 
        return this.idemp; 
    } 
    public void setIdemp(String idemp){ 
        this.idemp = idemp; 
    } 
 
    public Date getDateembauche(){ 
        return this.dateembauche; 
    } 
    public void setDateembauche(Date dateembauche){ 
        this.dateembauche = dateembauche; 
    } 
 
    public double getSalaire(){ 
        return this.salaire; 
    } 
    public void setSalaire(double salaire){ 
        this.salaire = salaire; 
    } 
 
}