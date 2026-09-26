package br.com.capitulando.model;

public class Historia {
    private long id;
    private String titulo;
    private String sinopsis;

    public Historia(long id, String titulo, String sinopsis) {
        this.id = id;
        setTitulo(titulo);
        setSinopsis(sinopsis);
    }
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        if(titulo == null || titulo.isBlank()) {
            System.out.println("O título não pode ser nulo ou vazio.");
        } 
        this.titulo = titulo;
    }
    public String getSinopsis() {
        return sinopsis;
    }
    public void setSinopsis(String sinopsis) {
        if(sinopsis == null || sinopsis.isBlank()) {
            System.out.println("A sinopse não pode ser nula ou vazia.");
        }   
        this.sinopsis = sinopsis;
    }
}