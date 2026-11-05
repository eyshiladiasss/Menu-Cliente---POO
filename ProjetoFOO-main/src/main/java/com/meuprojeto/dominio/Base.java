package com.meuprojeto.dominio;

import java.time.LocalDate;

public abstract class Base {

    private LocalDate dataCriacao;
    private String criadoPor;

    public Base() {
    }

    public Base(LocalDate dataCriacao, String criadoPor) {
        this.dataCriacao = dataCriacao;
        this.criadoPor = criadoPor;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }
}
