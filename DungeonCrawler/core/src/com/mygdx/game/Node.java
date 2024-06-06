package com.mygdx.game;


public class Node {
    private static int nextId = 0;
    public int id;
    public String name;

    public Node(String name){
        this.name = name;
        this.id = nextId++;
    }
}