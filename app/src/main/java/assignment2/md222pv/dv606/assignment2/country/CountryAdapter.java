package assignment2.md222pv.dv606.assignment2.country;

/**
 * Created by damma on 31.08.2016.
 */

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import assignment2.md222pv.dv606.assignment2.R;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.MyViewHolder> {

    private List<Country> countryList;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView title, year;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.name);
            year = (TextView) view.findViewById(R.id.year);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Edit country");
            menu.add(0, 0, 0, "Edit");
            menu.add(0, 1, 0, "Delete");
        }
    }

    public CountryAdapter(List<Country> countryList) {
        this.countryList = countryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Country country = countryList.get(position);
        holder.title.setText(country.getName());
        holder.year.setText(country.getYear());
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }
}
