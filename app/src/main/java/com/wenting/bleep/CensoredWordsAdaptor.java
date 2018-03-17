package com.wenting.bleep;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * Created by wenting on 3/12/18.
 */

public class CensoredWordsAdaptor extends BaseAdapter {
    private AddCensoredWordsActivity.ListProvider provider;
    private Context context;

    public CensoredWordsAdaptor(AddCensoredWordsActivity.ListProvider provider, Context context) {
        this.provider = provider;
        this.context = context;
    }

    private static class ViewHolder {
        TextView wordString;
        ImageView deleteIcon;
    }

    @Override
    public int getCount() {
        return provider.size();
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public String getItem(int i) {
        return provider.get(i);
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final String currentWord = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;  // view lookup cache stored in tag


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
            viewHolder.wordString = (TextView) convertView.findViewById(R.id.badword);
            viewHolder.deleteIcon = (ImageView) convertView.findViewById(R.id.item_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }



        viewHolder.wordString.setText(currentWord);
        viewHolder.deleteIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String deleteString = getCorrespondingWord(currentWord);
                provider.remove(deleteString);
            }

            private String getCorrespondingWord(String s) {
                switch (s) {
                    case "s**t":
                        return "shit";
                    case "p**s":
                        return "piss";
                    case "f***":
                        return "fuck";
                    case "c**t":
                        return "cunt";
                    case "coc*****er":
                        return "cocksucker";
                    case "motherf*****":
                        return "motherfucker";
                    case "t**s":
                        return "tits";
                    default:
                        return s;
                }
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }


}

