package com.example.pokedex;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokedexActivity extends AppCompatActivity {

    private EditText etPokemonSearch;
    private Button btnSearch;
    private CardView cardResult;
    private ImageView ivPokemon;
    private TextView tvPokemonName;
    private TextView tvPokemonType;
    private TextView tvBoasVindas;
    private View llSearchPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pokedex);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvBoasVindas = findViewById(R.id.tvBoasVindas);
        etPokemonSearch = findViewById(R.id.etPokemonSearch);
        btnSearch = findViewById(R.id.btnSearch);
        cardResult = findViewById(R.id.cardResult);
        ivPokemon = findViewById(R.id.ivPokemon);
        tvPokemonName = findViewById(R.id.tvPokemonName);
        tvPokemonType = findViewById(R.id.tvPokemonType);
        llSearchPlaceholder = findViewById(R.id.llSearchPlaceholder);

        String nome = getIntent().getStringExtra("TREINADOR_NOME");
        if (nome != null && !nome.trim().isEmpty()) {
            tvBoasVindas.setText(getString(R.string.boas_vindas_mensagem, nome));
        } else {
            tvBoasVindas.setText(getString(R.string.boas_vindas_padrao));
        }

        btnSearch.setOnClickListener(v -> {
            String query = etPokemonSearch.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                searchPokemon(query);
            } else {
                Toast.makeText(this, "Digite um nome ou número", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPokemon(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        btnSearch.setEnabled(false);

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/" + query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    result = sb.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResult = result;
            handler.post(() -> {
                btnSearch.setEnabled(true);
                if (finalResult != null) {
                    parseAndShowResult(finalResult);
                } else {
                    Toast.makeText(PokedexActivity.this, "Pokémon não encontrado", Toast.LENGTH_SHORT).show();
                    cardResult.setVisibility(View.GONE);
                    llSearchPlaceholder.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void parseAndShowResult(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String name = jsonObject.getString("name");
            
            if (name.length() > 0) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }

            JSONArray typesArray = jsonObject.getJSONArray("types");
            StringBuilder typesBuilder = new StringBuilder();
            for (int i = 0; i < typesArray.length(); i++) {
                JSONObject typeObj = typesArray.getJSONObject(i).getJSONObject("type");
                if (i > 0) typesBuilder.append(", ");
                String typeName = typeObj.getString("name");

                if (typeName.length() > 0) {
                    typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
                }
                typesBuilder.append(typeName);
            }

            String imageUrl = jsonObject.getJSONObject("sprites").getString("front_default");

            tvPokemonName.setText(getString(R.string.nome_pokemon) + " " + name);
            tvPokemonType.setText(getString(R.string.tipo_pokemon) + " " + typesBuilder.toString());
            
            Glide.with(this).clear(ivPokemon);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivPokemon);

            cardResult.setVisibility(View.VISIBLE);
            llSearchPlaceholder.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar dados", Toast.LENGTH_SHORT).show();
            cardResult.setVisibility(View.GONE);
            llSearchPlaceholder.setVisibility(View.VISIBLE);
        }
    }
}