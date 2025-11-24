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
import android.graphics.drawable.Drawable;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
    private TextView tvPokemonHeight;
    private TextView tvPokemonWeight;
    private TextView tvPokemonAbilities;
    private TextView tvPokemonStats;
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
        tvPokemonHeight = findViewById(R.id.tvPokemonHeight);
        tvPokemonWeight = findViewById(R.id.tvPokemonWeight);
        tvPokemonAbilities = findViewById(R.id.tvPokemonAbilities);
        tvPokemonStats = findViewById(R.id.tvPokemonStats);
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
            
            // Name
            String name = jsonObject.optString("name", "Unknown");
            if (name.length() > 0) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }

            // Types
            JSONArray typesArray = jsonObject.optJSONArray("types");
            StringBuilder typesBuilder = new StringBuilder();
            if (typesArray != null) {
                for (int i = 0; i < typesArray.length(); i++) {
                    JSONObject typeObj = typesArray.getJSONObject(i).getJSONObject("type");
                    if (i > 0) typesBuilder.append(", ");
                    String typeName = typeObj.getString("name");
                    if (typeName.length() > 0) {
                        typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
                    }
                    typesBuilder.append(typeName);
                }
            }

            // Height and Weight
            double height = jsonObject.optInt("height", 0) / 10.0;
            double weight = jsonObject.optInt("weight", 0) / 10.0;

            // Abilities
            JSONArray abilitiesArray = jsonObject.optJSONArray("abilities");
            StringBuilder abilitiesBuilder = new StringBuilder();
            if (abilitiesArray != null) {
                for (int i = 0; i < abilitiesArray.length(); i++) {
                    JSONObject abilityObj = abilitiesArray.getJSONObject(i).getJSONObject("ability");
                    if (i > 0) abilitiesBuilder.append(", ");
                    String abilityName = abilityObj.getString("name").replace("-", " ");
                    if (abilityName.length() > 0) {
                        abilityName = abilityName.substring(0, 1).toUpperCase() + abilityName.substring(1);
                    }
                    abilitiesBuilder.append(abilityName);
                }
            }

            // Stats
            JSONArray statsArray = jsonObject.optJSONArray("stats");
            StringBuilder statsBuilder = new StringBuilder();
            if (statsArray != null) {
                for (int i = 0; i < statsArray.length(); i++) {
                    JSONObject statObj = statsArray.getJSONObject(i);
                    int baseStat = statObj.getInt("base_stat");
                    String statName = statObj.getJSONObject("stat").getString("name");
                    
                    switch(statName) {
                        case "hp": statName = "HP"; break;
                        case "attack": statName = "ATK"; break;
                        case "defense": statName = "DEF"; break;
                        case "special-attack": statName = "SpA"; break;
                        case "special-defense": statName = "SpD"; break;
                        case "speed": statName = "SPD"; break;
                        default: statName = statName.toUpperCase();
                    }
                    
                    if (i > 0) statsBuilder.append("\n");
                    statsBuilder.append(statName).append(": ").append(baseStat);
                }
            }

            // Image Strategy: Official Art -> Home -> Default
            String imageUrl = null;
            JSONObject sprites = jsonObject.optJSONObject("sprites");
            if (sprites != null) {
                // Try Official Artwork
                JSONObject other = sprites.optJSONObject("other");
                if (other != null) {
                    JSONObject officialArt = other.optJSONObject("official-artwork");
                    if (officialArt != null) {
                        imageUrl = officialArt.optString("front_default", null);
                    }
                    // Try Home if Official Art failed
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        JSONObject home = other.optJSONObject("home");
                        if (home != null) {
                            imageUrl = home.optString("front_default", null);
                        }
                    }
                }
                // Fallback to default
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = sprites.optString("front_default", null);
                }
            }

            tvPokemonName.setText(name);
            tvPokemonType.setText(typesBuilder.toString());
            tvPokemonHeight.setText(getString(R.string.altura_pokemon, String.valueOf(height)));
            tvPokemonWeight.setText(getString(R.string.peso_pokemon, String.valueOf(weight)));
            tvPokemonAbilities.setText(abilitiesBuilder.toString());
            tvPokemonStats.setText(statsBuilder.toString());
            
            Glide.with(this).clear(ivPokemon);
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false; // Allow Glide to handle the error view
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(ivPokemon);
            } else {
                ivPokemon.setImageResource(R.drawable.ic_launcher_foreground);
            }

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