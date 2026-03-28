package com.example.analisedevendasapp;

import android.os.Bundle;
import android.widget.*;
import android.content.SharedPreferences;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class AnaliseActivity extends AppCompatActivity {

    // ==========================
    // COMPONENTES DA INTERFACE
    // ==========================
    Spinner spinnerRelatorio, spinnerCategoriaFiltro;
    Button botaoGerar, botaoVoltar;
    TextView textTotal, textCategoria, textRanking;

    // ==========================
    // DADOS
    // ==========================
    SharedPreferences preferences;
    ArrayList<String> produtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analise);

        // ==========================
        // CONEXÃO COM ELEMENTOS
        // ==========================
        spinnerRelatorio = findViewById(R.id.spinnerRelatorio);
        spinnerCategoriaFiltro = findViewById(R.id.spinnerCategoriaFiltro);
        botaoGerar = findViewById(R.id.botaoGerar);
        botaoVoltar = findViewById(R.id.botaoVoltar);

        textTotal = findViewById(R.id.textTotal);
        textCategoria = findViewById(R.id.textCategoria);
        textRanking = findViewById(R.id.textRanking);

        // ==========================
        // CONFIGURAÇÃO DOS SPINNERS
        // ==========================

        // Spinner de categorias (inclui opção geral)
        ArrayList<String> categorias = new ArrayList<>();
        categorias.add("Todas as categorias");

        String[] categoriasArray = getResources().getStringArray(R.array.categorias);
        categorias.addAll(Arrays.asList(categoriasArray));

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );

        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaFiltro.setAdapter(adapterCategoria);

        // Tipos de relatório
        String[] tipos = {
                "Resumo Geral",
                "Por Categoria",
                "Por Produto"
        };

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tipos
        );

        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelatorio.setAdapter(adapterTipo);

        // ==========================
        // CARREGAR DADOS SALVOS
        // ==========================
        preferences = getSharedPreferences("produtos", MODE_PRIVATE);
        produtos = new ArrayList<>();

        Set<String> set = preferences.getStringSet("lista", new HashSet<>());
        produtos.addAll(set);

        // Botão voltar
        botaoVoltar.setOnClickListener(v -> finish());

        // ==========================
        // GERAR RELATÓRIO
        // ==========================
        botaoGerar.setOnClickListener(v -> {

            String tipo = spinnerRelatorio.getSelectedItem().toString();
            String categoriaFiltro = spinnerCategoriaFiltro.getSelectedItem().toString();

            double totalLucro = 0;
            double totalCompra = 0;
            int quantidade = 0;

            double maiorLucro = Double.MIN_VALUE;
            String produtoMaisLucrativo = "";

            double menorLucro = Double.MAX_VALUE;
            String produtoMenorLucro = "";

            HashMap<String, Double> lucroPorProduto = new HashMap<>();
            HashMap<String, Integer> contagemCategorias = new HashMap<>();

            // ==========================
            // PROCESSAMENTO DOS DADOS
            // ==========================
            for (String item : produtos) {

                // Filtro por categoria
                if (!categoriaFiltro.equals("Todas as categorias")) {
                    if (!item.contains(categoriaFiltro)) continue;
                }

                try {
                    // EXTRAIR CATEGORIA
                    String categoria = item.split("\\|")[1].split("\n")[0].trim();

                    // Contagem de categorias
                    contagemCategorias.put(
                            categoria,
                            contagemCategorias.getOrDefault(categoria, 0) + 1
                    );

                    // EXTRAIR COMPRA
                    double compra = Double.parseDouble(
                            item.split("Compra: R\\$")[1].split("\\|")[0]
                                    .replace(",", ".").trim()
                    );

                    // EXTRAIR LUCRO
                    double lucro = Double.parseDouble(
                            item.split("Lucro: R\\$")[1]
                                    .replace(",", ".").trim()
                    );

                    String nomeProduto = item.split("\\|")[0].trim();

                    totalLucro += lucro;
                    totalCompra += compra;
                    quantidade++;

                    lucroPorProduto.put(nomeProduto, lucro);

                    // Produto com maior lucro
                    if (lucro > maiorLucro) {
                        maiorLucro = lucro;
                        produtoMaisLucrativo = nomeProduto;
                    }

                    // Produto com menor lucro
                    if (lucro < menorLucro) {
                        menorLucro = lucro;
                        produtoMenorLucro = nomeProduto;
                    }

                } catch (Exception e) {
                    // Ignora erros de formatação de dados
                }
            }

            // ==========================
            // CATEGORIA MAIS UTILIZADA
            // ==========================
            String categoriaMaisUsada = "-";
            int maiorQtd = 0;

            for (Map.Entry<String, Integer> entry : contagemCategorias.entrySet()) {
                if (entry.getValue() > maiorQtd) {
                    maiorQtd = entry.getValue();
                    categoriaMaisUsada = entry.getKey();
                }
            }

            // ==========================
            // MARGEM DE LUCRO
            // ==========================
            double margem = 0;
            if (totalCompra > 0) {
                margem = (totalLucro / totalCompra) * 100;
            }

            // ==========================
            // TOP 3 PRODUTOS
            // ==========================
            List<Map.Entry<String, Double>> listaOrdenada = new ArrayList<>(lucroPorProduto.entrySet());
            listaOrdenada.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            StringBuilder top3 = new StringBuilder();

            for (int i = 0; i < Math.min(3, listaOrdenada.size()); i++) {
                top3.append((i + 1)).append("º ")
                        .append(listaOrdenada.get(i).getKey())
                        .append(" (R$ ")
                        .append(String.format("%.2f", listaOrdenada.get(i).getValue()))
                        .append(")\n");
            }

            // ==========================
            // DEFINIÇÃO DE COR DO RESULTADO
            // ==========================
            if (totalLucro > 200) {
                textTotal.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (totalLucro > 50) {
                textTotal.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                textTotal.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // ==========================
            // EXIBIÇÃO DOS RESULTADOS
            // ==========================
            textTotal.setText(
                    "💰 Lucro: R$ " + String.format("%.2f", totalLucro) +
                            "\n📈 Margem: " + String.format("%.1f", margem) + "%"
            );

            textCategoria.setText("📦 Categoria mais usada: " + categoriaMaisUsada);

            textRanking.setText(
                    "📊 Quantidade de produtos: " + quantidade +
                            "\n\n🏆 Produto destaque:\n" + produtoMaisLucrativo +
                            "\n\n⚠️ Menor lucro:\n" + produtoMenorLucro +
                            "\n\n📈 Top 3 produtos:\n" + top3
            );
        });
    }
}