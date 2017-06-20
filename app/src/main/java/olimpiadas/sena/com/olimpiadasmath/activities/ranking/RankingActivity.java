package olimpiadas.sena.com.olimpiadasmath.activities.ranking;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import olimpiadas.sena.com.olimpiadasmath.R;
import olimpiadas.sena.com.olimpiadasmath.adapter.RankingAdapter;
import olimpiadas.sena.com.olimpiadasmath.model.User;

public class RankingActivity extends AppCompatActivity {

    View fragment;
    RecyclerView recyclerViewRanking;
    private List<User> users;
    RankingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ranking);
        getSupportActionBar().hide();
        recyclerViewRanking = (RecyclerView) findViewById(R.id.recycler_ranking);

        ((TextView)findViewById(R.id.txt_title_ranking)).setTypeface(Typeface.createFromAsset(this.getAssets(),"grobold.ttf"));

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewRanking.setLayoutManager(llm);

        User user = new User("yo", 10, 10);
        if (user.getPosition() < 100) {
            fragment = findViewById(R.id.fragment2);
            fragment.setVisibility(View.INVISIBLE);
        }

        llenarUsers();
        adapter = new RankingAdapter(users, this);
        recyclerViewRanking.setAdapter(adapter);
    }

    private void llenarUsers() {
        users = new ArrayList<>();
        users.add(new User("nick1", 100, 1));
        users.add(new User("nick2", 100, 2));
        users.add(new User("nick3", 100, 3));
        users.add(new User("nick4", 100, 4));
        users.add(new User("nick5", 100, 4));
    }
}
