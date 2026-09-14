package dev.douglaslira.sisvaleinterior.domain.model;

import dev.douglaslira.sisvaleinterior.domain.exception.CpfInvalidoException;
import dev.douglaslira.sisvaleinterior.domain.service.ValidadorCpf;

import java.util.Objects;

public final class Servidor {
    private final Long id;
    private final String nome;
    private final String cpf;
    private final String matricula;
    private final boolean ativo;

    // Construtor da classe Servidor com validação de CPF
    public Servidor(Long id, String nome, String matricula, String cpf, boolean ativo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do servidor não pode ser nulo ou vazio.");
        }
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("A matrícula do servidor não pode ser nula ou vazia.");
        }
        if (!ValidadorCpf.isValido(cpf)) {
            throw new CpfInvalidoException();
        }
        this.id = id;
        this.nome = nome.trim(); 
        this.matricula = matricula.trim(); 
        this.cpf = ValidadorCpf.normalizar(cpf); 
        this.ativo = ativo; 
    }

    
    public static Servidor novo(String nome, String matricula, String cpf) {
        return new Servidor(null, nome, matricula, cpf, true);
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String matricula() {
        return matricula;
    }

    public String cpf() {
        return cpf;
    }

    public boolean ativo() {
        return ativo;
    }

    /* 
    * Sobrescreve o método equals para comparar objetos Servidor com base na matrícula, garantindo que dois servidores com a mesma matrícula sejam considerados iguais.
    */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Servidor outro)) return false;
        return matricula.equals(outro.matricula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricula);
    }

    @Override
    public String toString() {
        return "Servidor{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", matricula='" + matricula + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
