package ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.INotificationSideChannel;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appdevsoumitri.quickchatapp.JournalListActivity;
import com.appdevsoumitri.quickchatapp.JournalModel;
import com.appdevsoumitri.quickchatapp.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder>
{
    private Context context;
    private ArrayList<JournalModel> journalList;

    public JournalRecyclerAdapter(Context context, ArrayList<JournalModel> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context)
                .inflate(R.layout.journal_row,parent,false);

        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull final JournalRecyclerAdapter.ViewHolder holder, int position) {

        JournalModel journal=journalList.get(position);
        final String imageUrl;

        holder.title.setText(journal.getTitle());
        holder.thought.setText(journal.getThought());
        holder.name.setText(journal.getUsername());
        imageUrl=journal.getImageUrl();


        /**
         * to have timestamp like "1 hr ago" / "26 minutes ago" etc
         * link: https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
         */
        String timeAgo= (String) DateUtils.getRelativeTimeSpanString(journal
                .getTimeAdded().getSeconds()*1000);

        holder.dateAdded.setText(timeAgo);



        // using picasso library to download and show the image
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.wallpaper)
                .fit()
                .into(holder.image);

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sharing option
                // new JournalListActivity().shareFunction(title.toString(),thought.toString());
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                String shareBody = "Title: "+holder.title.getText().toString().trim()+"\n\nPost: "+holder.thought.getText().toString().trim()+"\n\nWritten by: "+holder.name.getText().toString().trim();

                Log.d("Sharable:",shareBody);
                Log.d("Image URL ",imageUrl);

                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Blog Post");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                //Uri imageUri=Uri.parse(imageUrl);
                //sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

                /*
                downloading and getting the image
                Bitmap bitmap;
                try {
                    URL url=new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream in = (InputStream) new URL(imageUrl).getContent();
                    bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    Log.d("Image downloaded ", "true");
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Image source not found, ensure you have proper network connection", Toast.LENGTH_SHORT).show();
                }
                */

                sharingIntent.setType("text/*");
                context.startActivity(Intent.createChooser(sharingIntent, "Share via..."));

                Log.d("Status: ","Sharing file");
            }
        });

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title,thought,dateAdded,name;
        public ImageView image;
        public Button btnShare;
        String userID, username;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context=ctx;

            title=itemView.findViewById(R.id.display_title);
            thought=itemView.findViewById(R.id.display_thought);
            dateAdded=itemView.findViewById(R.id.journal_timestamp_list);
            image=itemView.findViewById(R.id.journal_image_list);
            name=itemView.findViewById(R.id.journal_row_name);
            btnShare=itemView.findViewById(R.id.btnShare);

            /*btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // sharing option
                    // new JournalListActivity().shareFunction(title.toString(),thought.toString());
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "Title: "+title.toString().trim()+"\nPost: "+thought.toString().trim()+"\n";
                    Log.d("Sharable:",shareBody);
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Blog Post");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    Log.d("Status: ","Sharing file");
                }
            });*/
        }
    }
}


