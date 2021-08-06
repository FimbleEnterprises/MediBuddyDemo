package com.fimbleenterprises.medimileage.objects_and_containers;

import com.fimbleenterprises.medimileage.adapters.BasicObjectsExpandableListviewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> cricket = new ArrayList<String>();
        cricket.add("India");
        cricket.add("Pakistan");
        cricket.add("Australia");
        cricket.add("England");
        cricket.add("South Africa");

        List<String> football = new ArrayList<String>();
        football.add("Brazil");
        football.add("Spain");
        football.add("Germany");
        football.add("Netherlands");
        football.add("Italy");

        List<String> basketball = new ArrayList<String>();
        basketball.add("United States");
        basketball.add("Spain");
        basketball.add("Argentina");
        basketball.add("France");
        basketball.add("Russia");

        expandableListDetail.put("CRICKET TEAMS", cricket);
        expandableListDetail.put("FOOTBALL TEAMS", football);
        expandableListDetail.put("BASKETBALL TEAMS", basketball);
        return expandableListDetail;
    }

    public static ArrayList<BasicObjectsExpandableListviewAdapter.BasicObjectGroup> getBasicObjectGroupsPseudoData() {
        ArrayList<BasicObjectsExpandableListviewAdapter.BasicObjectGroup> groups = new ArrayList<>();

        BasicObjectsExpandableListviewAdapter.BasicObjectGroup parent1 = new BasicObjectsExpandableListviewAdapter.BasicObjectGroup();
        parent1.title = "Parent 1 and it's progeny";
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test1", "subtitle1", null));
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test2", "subtitle2", null));
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test3", "subtitle3", null));
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test4", "subtitle4", null));
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test5", "subtitle5", null));
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test6", "subtitle6", null));
        parent1.children.add(new BasicObjects.BasicObject("parent 1 test7", "subtitle7", null));
        groups.add(parent1);

        BasicObjectsExpandableListviewAdapter.BasicObjectGroup parent2 = new BasicObjectsExpandableListviewAdapter.BasicObjectGroup();
        parent2.title = "Parent 2 and it's progeny";
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test1", "subtitle1", null));
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test2", "subtitle2", null));
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test3", "subtitle3", null));
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test4", "subtitle4", null));
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test5", "subtitle5", null));
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test6", "subtitle6", null));
        parent2.children.add(new BasicObjects.BasicObject("parent 2 test7", "subtitle7", null));
        groups.add(parent2);

        BasicObjectsExpandableListviewAdapter.BasicObjectGroup parent3 = new BasicObjectsExpandableListviewAdapter.BasicObjectGroup();
        parent3.title = "Parent 3 and it's progeny";
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test1", "subtitle1", null));
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test2", "subtitle2", null));
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test3", "subtitle3", null));
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test4", "subtitle4", null));
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test5", "subtitle5", null));
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test6", "subtitle6", null));
        parent3.children.add(new BasicObjects.BasicObject("parent 3 test7", "subtitle7", null));
        groups.add(parent3);

        BasicObjectsExpandableListviewAdapter.BasicObjectGroup parent4 = new BasicObjectsExpandableListviewAdapter.BasicObjectGroup();
        parent4.title = "Parent 4 and it's progeny";
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test1", "subtitle1", null));
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test2", "subtitle2", null));
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test3", "subtitle3", null));
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test4", "subtitle4", null));
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test5", "subtitle5", null));
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test6", "subtitle6", null));
        parent4.children.add(new BasicObjects.BasicObject("parent 4 test7", "subtitle7", null));
        groups.add(parent4);

        return groups;
    }
}
