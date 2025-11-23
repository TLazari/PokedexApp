package com.example.pokedex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText editNome = findViewById(R.id.editNome);
        Button botaoEntrar = findViewById(R.id.botaoEntrar);
        botaoEntrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString().trim();
            if (!nome.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, PokedexActivity.class);
                intent.putExtra("TREINADOR_NOME", nome);
                startActivity(intent);
            }
        });
    }
}