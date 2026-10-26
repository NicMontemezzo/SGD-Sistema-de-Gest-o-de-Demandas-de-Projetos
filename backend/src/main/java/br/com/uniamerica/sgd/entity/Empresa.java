package br.com.uniamerica.sgd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    // Lazy e fora do EmpresaResponse: a lista de demandas nunca vai na resposta da API
    @OneToMany(mappedBy = "empresa")
    private List<Demanda> demandas = new ArrayList<>();
}
