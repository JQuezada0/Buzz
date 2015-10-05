package moby.mobyv02.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import moby.mobyv02.ClusterRenderer;
import moby.mobyv02.R;
import moby.mobyv02.parse.Post;

/**
 * Created by quezadjo on 9/20/2015.
 */
public class MapTree {

    private final LinkedList<Node> nodes = new LinkedList<Node>();
    private final ArrayList<Post> posts;
    private final Context context;
    private final ClusterManager<Post> clusterManager;
    private final ClusterRenderer clusterRenderer;
    private final GoogleMap map;
    private int position = 0;
    private final int maxNodes;
    private boolean creation = true;
    protected static Bitmap missingProfileImage;

    private MapTree(Context context, ClusterManager<Post> clusterManager, ClusterRenderer clusterRenderer, GoogleMap map, ArrayList<Post> posts){

        this.clusterManager = clusterManager;
        this.clusterRenderer = clusterRenderer;
        this.posts = posts;
        this.context = context;
        this.map = map;
        this.maxNodes = calculateMaxNodes();
        this.missingProfileImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_icon_graybg);
    }

    private void calculateTree(Post initialPost){
        startTree(initialPost);
        System.out.println("Tree creation has begun. The current amount of posts is " + posts.size() + ". The max Nodes is " + maxNodes + ".");
        while (position < maxNodes - 1){
            traverseForward();
        }
        position = 0;
        resetAllNodes();
        updateMap();
        creation = false;
    }

    public static MapTree createTree(Context context, ClusterManager<Post> clusterManager, ClusterRenderer clusterRenderer, GoogleMap map, ArrayList<Post> posts, Post initialPost){

        MapTree mapTree = new MapTree(context, clusterManager, clusterRenderer, map, posts);
        mapTree.calculateTree(initialPost);
        return mapTree;
    }

    private void startTree(Post post){
        System.out.println(post.getUser().getString("profileImage"));
        Node firstNode = new Node(this, 0).createFromPost(post);
        nodes.add(firstNode);
        updateMap();
    }

    public void traverseForward(){

        Node currentNode = getCurrentNode();
        if (currentNode.forwardTraversalPossible()){
            currentNode.traverseForward();
        } else {
            traverseForwardFromEndOfNode();
        }

    }

    public void traverseBackward(){

        Node currentNode = getCurrentNode();
        if (currentNode.backwardsTraversalPossible()){
            currentNode.traverseBackward();
        } else {
            traverseBackwardFromEndOfNode();
        }

    }

    private void traverseForwardFromEndOfNode(){
        getCurrentNode().exitNode();
        if (forwardTraversalPossible()){
            traverseToNextNode();
        } else {
            traverseForwardFromEndOfTree();
        }
    }

    private void traverseBackwardFromEndOfNode(){
        getCurrentNode().exitNode();
        if (backwardsTraversalPossible()){
            traverseToPreviousNode();
        } else {
            //End of tree reached
        }
    }

    private void traverseToNextNode(){
        System.out.println("Traverse to next node");
        position++;
        updateMap();
        getCurrentNode().updateMarker();
    }
    private void traverseToPreviousNode(){
        position--;
        updateMap();
        getCurrentNode().updateMarker();
    }

    private void traverseForwardFromEndOfTree(){
        System.out.println("Traverse forward from end of tree");
        addNewNode();
        traverseToNextNode();
    }

    private void updateMap(){
        if (!creation) {
            Node currentNode = getCurrentNode();
            Marker marker = currentNode.getMarker();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), 13.0f));
        }
    }

    private void addNewNode() {
        System.out.println("add new node");
        nodes.add(findNextNode());
    }

    private Node findNextNode(){

        Node node = getCurrentNode();
        ArrayList<Post> sortedPosts = getSortedPosts(node.getCurrentPost());
        Node newNode = new Node(this, position);
        newNode.createFromPost(sortedPosts.get(0));
        return newNode;

    }

    private Node getCurrentNode(){
        return nodes.get(position);
    }

    private boolean forwardTraversalPossible() {
        System.out.println("Is forward traversal possible? position is " + position + ". Posts size is " + posts.size());
        if (position == (nodes.size() - 1))
            return false;
        return true;
    }

    private boolean backwardsTraversalPossible() {
        if (position == 0)
            return false;
        return true;
    }

    private ArrayList<Post> getSortedPosts(Post post){

        ArrayList<Post> sortedPostsList = new ArrayList<>(posts);
        sortedPostsList.removeAll(getAllTraversedPosts());
        Collections.sort(sortedPostsList);
        return sortedPostsList;

    }

    protected ClusterManager<Post> getClusterManager(){
        return clusterManager;
    }

    protected ClusterRenderer getClusterRenderer(){
        return clusterRenderer;
    }

    private ArrayList<Post> getAllTraversedPosts(){

        ArrayList<Post> traversedPosts = new ArrayList<Post>();

        for (Node node : nodes){
            traversedPosts.addAll(node.getPosts());
        }

        return traversedPosts;
    }

    protected Context getContext(){
        return context;
    }

    private int calculateMaxNodes(){
        int clusterMarkerCollectionSize = clusterManager.getClusterMarkerCollection().getMarkers().size();
        System.out.println("Visible clusters is " + clusterMarkerCollectionSize);
        int clusterItemMarkerCollectionSize = clusterManager.getMarkerCollection().getMarkers().size();
        System.out.println("Visible cluster items is " + clusterItemMarkerCollectionSize);
        return clusterMarkerCollectionSize + clusterItemMarkerCollectionSize;
    }

    public ArrayList<Post> getNewPosts(){
        return getAllTraversedPosts();
    }

    public void resetAllNodes(){
        for (Node node : nodes){
            node.setPosition(0);
        }
    }

    protected boolean getCreation(){
        return creation;
    }

}
