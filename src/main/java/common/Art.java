package common;
import java.time.LocalDateTime;

public class Art extends Item{
    private static final long serialVersionUID = 1L;
    private String artist;

    public Art() {
        super();
    }

    public Art(String itemName, String description, double startingPrice, LocalDateTime startingTime, LocalDateTime closingTime, User owner, String artist){
        super(itemName,description,startingPrice,startingTime,closingTime,owner);
        this.artist = artist;
    }

    public void setArtist(String name){this.artist = name;}
    public String getArtist(){return artist;}

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
             +"\nDescription: "+ this.getDescription()
             +"\nOwner: " + this.getOwner()
             +"\nArtist: " + this.getArtist();
    }
}
