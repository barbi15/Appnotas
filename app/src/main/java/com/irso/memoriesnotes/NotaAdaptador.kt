package com.irso.memoriesnotes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class NotaAdaptador(private val context: Context, private val notas: List<Nota>, private var query: String = "") : BaseAdapter() {
    //tamaño
    override fun getCount(): Int {
        return notas.size
    }
    //posicion
    override fun getItem(position: Int): Any {
        return notas[position]
    }
    //ID
    override fun getItemId(position: Int): Long {
        return notas[position].id.toLong()
    }
    //proporciona una vista para cada elemento
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_nota, parent, false)
        val tituloTextView: TextView = view.findViewById(R.id.tituloTextView)
        val contenidoTextView: TextView = view.findViewById(R.id.contenidoTextView)
//obtengo elemento
        val nota = notas[position]
        tituloTextView.text = nota.titulo
        contenidoTextView.text = nota.contenido
                // Resaltar si la nota coincide con la consulta
                if (query.isNotEmpty() && (nota.titulo.contains(query, ignoreCase = true) || nota.contenido.contains(query, ignoreCase = true))) {
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.highlight))
                } else {
                    view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                }

        return view
    }

    fun setQuery(query: String) {
        this.query = query
        notifyDataSetChanged()
    }
}