package org.gregsquad.gregsadventure.card;

import java.util.Random;
import org.gregsquad.gregsadventure.game.Player;
import java.io.Serializable;

public class Curse extends Card implements Serializable{
    private String type;
    private int value;

    public Curse(int id, String name, String description, String type, int value){
        super(id, name, description);
        this.type = type;
        this.value = value;
    }

    public void curse(Player player){
        if(type == "level"){
            player.addLevel(value);
        }
        if(type == "damage"){
            player.addDamage(value);
        }
        if(type == "equipement"){
            Random rand = new Random();
            player.getStuff().removeEquipement(rand.nextInt(player.getStuff().getSize()));
        }
    }

    public final void play() {
        System.out.println("Playing curse " + this.name);
        //Ask player to choose a curse
        //Interface choix joueur
        Player playerSelected = null;
        //curse(playerSelected);
        }

}
