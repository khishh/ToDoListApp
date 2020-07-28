package com.example.todo.ui.home.itemmanagementfragment;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.todo.model.Tab;
import com.example.todo.model.TabToDoDao;
import com.example.todo.model.TabToDoDataBase;
import com.example.todo.model.ToDo;

import java.util.ArrayList;
import java.util.List;

public class ItemManagementViewModel extends AndroidViewModel {

    private static final String TAG = "ItemManagementViewModel";

    private MutableLiveData<List<ToDo>> mDoList = new MutableLiveData<>();
    private MutableLiveData<List<Tab>> tabs = new MutableLiveData<>();
    private MutableLiveData<Integer> mTabId = new MutableLiveData<>();

    private AsyncTask<Integer, Void, List<ToDo>> retrieveToDoFromDatabase;

    // the instance of UpdateTabIntoDatabase class extends to AsyncTask to save all UPDATED Tabs into Room Database
    private AsyncTask<List<ToDo>, Void, Void> updateToDoList;

    private AsyncTask<List<ToDo>, Void, Void> removeDoneToDo;

    private AsyncTask<Void, Void, List<Tab>> retrieveTabsFromDatabase;

    private AsyncTask<Integer, Void, List<ToDo>> insertToDoIntoTab;

    public ItemManagementViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadToDoList(int tabId){
        mTabId.setValue(tabId);
        retrieveToDoFromDatabase = new RetrieveToDoTask();
        retrieveToDoFromDatabase.execute(tabId);
    }

    public void loadTabs(){
        retrieveTabsFromDatabase = new RetrieveTabsFromDataBase();
        retrieveTabsFromDatabase.execute();
    }

    public void updateToDoList(int fromPos, int toPos){
        ToDo fromToDo = mDoList.getValue().get(fromPos);
        ToDo toToDo = mDoList.getValue().get(toPos);

        swapToDoList(fromToDo, toToDo);

        List<ToDo> tabsUpdated = new ArrayList<>();
        tabsUpdated.add(fromToDo);
        tabsUpdated.add(toToDo);

        updateToDoList = new UpdateToDo();
        updateToDoList.execute(tabsUpdated);
    }

    public void deleteSelectedToDo(){
        List<ToDo> doneToDos = removeAllDoneToDos();

        removeDoneToDo = new RemoveDoneToDo();
        removeDoneToDo.execute(doneToDos);
    }

    public void moveToDoToOtherTab(int targetTabId){
        insertToDoIntoTab = new InsertToDoTask();
        insertToDoIntoTab.execute(targetTabId);
    }

    private class RetrieveToDoTask extends AsyncTask<Integer, Void, List<ToDo>>{

        @Override
        protected List<ToDo> doInBackground(Integer... integers) {
            int tabId = integers[0];

            TabToDoDao dao = TabToDoDataBase.getInstance(getApplication()).tabToDoDao();
            List<ToDo> toDoList = dao.getToDoList(tabId);

            return toDoList;
        }

        @Override
        protected void onPostExecute(List<ToDo> toDos) {
            toDoListRetrieved(toDos);
            Log.d(TAG, "data loaded");
            Toast.makeText(getApplication(), "ToDoList retrieved from your database", Toast.LENGTH_SHORT).show();
        }
    }

    private void toDoListRetrieved(List<ToDo> toDos){
        mDoList.setValue(toDos);
    }

    private class UpdateToDo extends AsyncTask<List<ToDo>, Void, Void>{

        @Override
        protected Void doInBackground(List<ToDo>... lists) {

            TabToDoDao dao = TabToDoDataBase.getInstance(getApplication()).tabToDoDao();
            dao.updateToDoList(lists[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplication(), "ToDoList updated into your database", Toast.LENGTH_SHORT).show();
        }
    }

    private class RemoveDoneToDo extends AsyncTask<List<ToDo>, Void, Void>{

        @Override
        protected Void doInBackground(List<ToDo>... lists) {
            TabToDoDao dao = TabToDoDataBase.getInstance(getApplication()).tabToDoDao();
            dao.deleteToDos(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class RetrieveTabsFromDataBase extends AsyncTask<Void, Void, List<Tab>>{

        @Override
        protected List<Tab> doInBackground(Void... voids) {
            return TabToDoDataBase.getInstance(getApplication()).tabToDoDao().getAllTab();
        }

        @Override
        protected void onPostExecute(List<Tab> tabs) {
            tabsRetrieved(tabs);
            Log.d(TAG, "RETRIEVED " + tabs.toString());
//            Toast.makeText(getApplication(), "Data retrieved from your local data", Toast.LENGTH_LONG).show();
        }
    }

    private class InsertToDoTask extends AsyncTask<Integer, Void, List<ToDo>>{

        @Override
        protected List<ToDo> doInBackground(Integer... integers) {

            Log.d(TAG, "InsertToDoTask started");
            TabToDoDao dao = TabToDoDataBase.getInstance(getApplication()).tabToDoDao();

            int targetTabId = integers[0];
            List<ToDo> curList = mDoList.getValue();
            List<ToDo> insertList = new ArrayList<>();

            Log.d(TAG, curList.toString());

            int i = 0;

            while(i < curList.size()){
                ToDo todo = curList.get(i);
                if(todo.isDone()) {
                    Log.d(TAG, todo.toString());
                    todo.setToDoOwnerId(targetTabId);
                    todo.setDone(false);
                    insertList.add(todo);
                    curList.remove(todo);
                }
                else
                    ++i;
            }

            Log.d(TAG, insertList.toString());
            dao.updateToDoList(insertList);
            return curList;
        }

        @Override
        protected void onPostExecute(List<ToDo> toDos) {
            toDoListRetrieved(toDos);
        }
    }

    private void tabsRetrieved(List<Tab> tabs) {
        this.tabs.setValue(tabs);
    }

    public void swapToDoList(ToDo fromToDo, ToDo toToDo){
        String contentKeep = fromToDo.getContent();
        boolean isDoneKeep = fromToDo.isDone();

        fromToDo.setDone(toToDo.isDone());
        fromToDo.setContent(toToDo.getContent());

        toToDo.setDone(isDoneKeep);
        toToDo.setContent(contentKeep);
    }

    public List<ToDo> removeAllDoneToDos(){
        List<ToDo> curList = mDoList.getValue();
        List<ToDo> doneList = new ArrayList<>();

        int i = 0;
        while(i < curList.size()){
            if(curList.get(i).isDone()){
                doneList.add(curList.get(i));
                curList.remove(i);
            }
            else ++i;
        }

        mDoList.setValue(curList);
        return doneList;
    }

    public MutableLiveData<List<ToDo>> getmDoList() {
        return mDoList;
    }

    public List<Tab> getTabList(){
        return tabs.getValue();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if(retrieveToDoFromDatabase != null){
            retrieveToDoFromDatabase.cancel(true);
            retrieveToDoFromDatabase = null;
        }

        if(removeDoneToDo != null){
            removeDoneToDo.cancel(true);
            removeDoneToDo = null;
        }

        if(updateToDoList != null){
            updateToDoList.cancel(true);
            updateToDoList = null;
        }

        if(retrieveTabsFromDatabase != null){
            retrieveTabsFromDatabase.cancel(true);
            retrieveTabsFromDatabase = null;
        }

        if(insertToDoIntoTab != null){
            insertToDoIntoTab.cancel(true);
            insertToDoIntoTab = null;
        }
    }
}