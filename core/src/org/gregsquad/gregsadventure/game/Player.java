package org.gregsquad.gregsadventure.game;

import java.util.Random;
import java.io.Serializable;

import org.gregsquad.gregsadventure.card.Card;
import org.gregsquad.gregsadventure.card.CardList;
import org.gregsquad.gregsadventure.card.Deck;
import org.gregsquad.gregsadventure.card.Race;
import org.gregsquad.gregsadventure.card.Class;

public class Player implements Serializable{
    private String name;
    private int level = 0;
    private int damage = 0;

    private Class classe;
    private Race race; 
    private Deck deck;

    private Stuff stuff;

    private int diceBuff;
    private int treasuresForFight;

    public Player(String name){
        this.name = name;
        this.level = 1;
        this.damage = 1;
        this.deck = new Deck();
        this.stuff = new Stuff();
        this.diceBuff = 0;
    }

    public String getName(){
        return this.name;
    }

    public int getDiceBuff(){
        //check if we have a dice buff in classes
        //TODO
        return this.diceBuff;
    }

    public void setDiceBuff(int diceBuff){
        this.diceBuff = diceBuff;
    }
    public int getTreasuresForFight(){
        return this.treasuresForFight;
    }

    public void setTreasuresForFight(int treasures){
        this.treasuresForFight = treasures;
    }

    public int getLevel(){
        return this.level;
    }

    public void addLevel(int level){
        this.level += level;
    }

    public int getDamage(){//We do the calcul of damage bonus here
        damage = level;
        //TODO




        return this.damage;
    }

    public void setDamage(int damage){
        this.damage = damage;
    }

    public void addDamage(int damage){
        this.damage += damage;
    }

 


    public Class getClasse(){
        return this.classe;
    }

    public Race getRace(){
        return this.race;
    }

    public Deck getDeck(){
        return this.deck;
    }

    public Stuff getStuff(){
        return this.stuff;
    }
}
