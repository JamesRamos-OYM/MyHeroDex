package com.example.proyectofinal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SuperHeroAdapter extends RecyclerView.Adapter<SuperHeroAdapter.ViewHolder> {
    private List<SuperHero> lista;

    public SuperHeroAdapter(List<SuperHero> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_superhero, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(lista.get(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        TextView txtDescripcion;
        ImageView imgHero;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_hero);
            txtDescripcion = itemView.findViewById(R.id.txt_descripcion_hero);
            imgHero = itemView.findViewById(R.id.img_hero);
            context = itemView.getContext();
        }

        public void bind(SuperHero hero) {
            txtNombre.setText(hero.getNombre());
            txtDescripcion.setText("Toca para ver más detalles");

            // Cargar imagen con Picasso
            Picasso.get()
                    .load(hero.getImagenUrl())
                    .placeholder(R.drawable.ic_hero_placeholder)
                    .error(R.drawable.ic_hero_error)
                    .into(imgHero);

            // Click listener para abrir detalles - AHORA PASA TODA LA INFORMACIÓN
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetalleActivity.class);
                intent.putExtra("nombre", hero.getNombre());
                intent.putExtra("imagen", hero.getImagenUrl());
                intent.putExtra("descripcion", hero.getDescripcion());
                intent.putExtra("id", hero.getId());
                context.startActivity(intent);
            });
        }
    }
}