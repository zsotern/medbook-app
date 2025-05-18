package com.example.medbook.model;

public class Specialty {
    private String id;           // Egyedi azonosító
    private String name;         // Specialitás neve (pl. Kardiológia)
    private int imageResource;

    public Specialty() {}

    public Specialty(String id, String name, int imageResource) {
        this.id = id;
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getImageResource() {return imageResource; }

}