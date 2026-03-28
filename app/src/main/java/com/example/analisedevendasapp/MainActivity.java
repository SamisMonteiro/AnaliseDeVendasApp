package com.example.analisedevendasapp;

import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // ==========================
    // ARMAZENAMENTO LOCAL
    // ==========================
    // Responsável por manter os dados salvos no dispositivo
    SharedPreferences preferences;

    // ==========================
    // COMPONENTES DA INTERFACE
    // ==========================
    EditText editNome, editPrecoCompra, editPrecoVenda;
    Button botaoSalvar, botaoVerProdutos, botaoAnalise;
    Spinner spinnerCategoria;

    // ==========================
    // LISTA DE DADOS
    // ==========================
    ArrayList<String> produtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o armazenamento
        preferences = getSharedPreferences("produtos", MODE_PRIVATE);

        // ==========================
        // CONECTAR ELEMENTOS DA TELA
        // ==========================
        editNome = findViewById(R.id.editNome);
        editPrecoCompra = findViewById(R.id.editPrecoCompra);
        editPrecoVenda = findViewById(R.id.editPrecoVenda);

        botaoSalvar = findViewById(R.id.botaoSalvar);
        botaoVerProdutos = findViewById(R.id.botaoVerProdutos);
        botaoAnalise = findViewById(R.id.botaoAnalise);

        spinnerCategoria = findViewById(R.id.spinnerCategoria);

        // ==========================
        // CONFIGURAR SPINNER DE CATEGORIAS
        // ==========================
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(
                this,
                R.array.categorias,
                android.R.layout.simple_spinner_item
        );

        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // ==========================
        // CARREGAR PRODUTOS SALVOS
        // ==========================
        produtos = new ArrayList<>();
        carregarProdutos();

        // ==========================
        // BOTÃO SALVAR PRODUTO
        // ==========================
        botaoSalvar.setOnClickListener(v -> {

            String nome = editNome.getText().toString();
            String categoria = spinnerCategoria.getSelectedItem().toString();

            String precoCompra = editPrecoCompra.getText().toString().replace(",", ".");
            String precoVenda = editPrecoVenda.getText().toString().replace(",", ".");

            // Validação dos campos
            if (nome.isEmpty() || precoCompra.isEmpty() || precoVenda.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Conversão e cálculo
            double compra = Double.parseDouble(precoCompra);
            double venda = Double.parseDouble(precoVenda);
            double lucro = venda - compra;

            // Montagem do produto
            String produto = nome + " | " + categoria +
                    "\nCompra: R$" + precoCompra.replace(".", ",") +
                    " | Venda: R$" + precoVenda.replace(".", ",") +
                    " | Lucro: R$" + String.format("%.2f", lucro).replace(".", ",");

            // Salvar produto
            produtos.add(produto);
            salvarProdutos();

            // Feedback para o usuário
            Toast.makeText(this, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show();

            // Limpar campos
            editNome.setText("");
            editPrecoCompra.setText("");
            editPrecoVenda.setText("");
        });

        // ==========================
        // NAVEGAÇÃO ENTRE TELAS
        // ==========================

        // Tela de lista completa
        botaoVerProdutos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaProdutosActivity.class);
            startActivity(intent);
        });

        // Tela de análise
        botaoAnalise.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AnaliseActivity.class);
            startActivity(intent);
        });
    }

    // ==========================
    // SALVAR DADOS
    // ==========================
    // Armazena a lista no SharedPreferences
    private void salvarProdutos() {
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set = new HashSet<>(produtos);
        editor.putStringSet("lista", set);
        editor.apply();
    }

    // ==========================
    // CARREGAR DADOS
    // ==========================
    // Recupera os produtos ao iniciar o app
    private void carregarProdutos() {
        Set<String> set = preferences.getStringSet("lista", new HashSet<>());
        produtos.addAll(set);
    }
}