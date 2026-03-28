package com.example.analisedevendasapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class ListaProdutosActivity extends AppCompatActivity {

    // ==========================
    // COMPONENTES DA INTERFACE
    // ==========================
    ListView listaProdutos;
    Button botaoVoltar;

    // ==========================
    // DADOS
    // ==========================
    ArrayList<String> produtos;
    ArrayAdapter<String> adapter;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);

        // ==========================
        // CONECTAR ELEMENTOS
        // ==========================
        listaProdutos = findViewById(R.id.listaProdutos);
        botaoVoltar = findViewById(R.id.botaoVoltar);

        // ==========================
        // CARREGAR DADOS SALVOS
        // ==========================
        preferences = getSharedPreferences("produtos", MODE_PRIVATE);

        Set<String> set = preferences.getStringSet("lista", new HashSet<>());

        produtos = new ArrayList<>();

        // ==========================
        // ORGANIZAÇÃO POR CATEGORIA
        // ==========================
        HashMap<String, ArrayList<String>> mapa = new HashMap<>();

        for (String item : set) {
            try {
                // Extrai categoria (após "|")
                String[] partes = item.split("\\|");

                if (partes.length > 1) {
                    String categoria = partes[1].split("\n")[0].trim();

                    if (!mapa.containsKey(categoria)) {
                        mapa.put(categoria, new ArrayList<>());
                    }

                    mapa.get(categoria).add(item);
                }

            } catch (Exception e) {
                // Ignora possíveis erros de formatação
            }
        }

        // ==========================
        // ORDENAR CATEGORIAS (A-Z)
        // ==========================
        ArrayList<String> categoriasOrdenadas = new ArrayList<>(mapa.keySet());
        Collections.sort(categoriasOrdenadas);

        // ==========================
        // MONTAR LISTA FINAL
        // ==========================
        for (String categoria : categoriasOrdenadas) {

            // Adiciona título da categoria
            produtos.add("📦 " + categoria.toUpperCase());

            // Adiciona produtos da categoria
            produtos.addAll(mapa.get(categoria));
        }

        // ==========================
        // CONFIGURAR LISTA
        // ==========================
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                produtos
        );

        listaProdutos.setAdapter(adapter);

        // ==========================
        // BOTÃO VOLTAR
        // ==========================
        botaoVoltar.setOnClickListener(v -> finish());

        // Ativar seta na barra superior (se disponível)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // ==========================
    // AÇÃO DA SETA SUPERIOR
    // ==========================
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}