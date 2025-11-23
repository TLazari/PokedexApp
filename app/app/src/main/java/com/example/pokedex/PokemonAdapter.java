package com.example.pokedex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private List<Pokemon> pokemonList;

    public PokemonAdapter(List<Pokemon> pokemonList) {
        this.pokemonList = pokemonList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pokemon pokemon = pokemonList.get(position);
        String nameCap = pokemon.name.substring(0, 1).toUpperCase() + pokemon.name.substring(1);
        holder.tvName.setText(nameCap);
        holder.tvType.setText(pokemon.type);
        Glide.with(holder.itemView.getContext())
                .load(pokemon.imageUrl)
                .into(holder.ivPokemon);
    }

    @Override
    public int getItemCount() {
        return pokemonList != null ? pokemonList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPokemon;
        TextView tvName;
        TextView tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPokemon = itemView.findViewById(R.id.ivPokemon);
            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}