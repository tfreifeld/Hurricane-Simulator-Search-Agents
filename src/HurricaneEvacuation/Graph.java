package HurricaneEvacuation;

import java.io.*;
import java.util.*;

class Graph {

    static final String edgeEncoding = "#E";
    static final String vertexEncoding = "#V";
    static final String deadlineEncoding = "#D";
    static final String ignoreNonDigitsRegex = "[^0-9]*";

    private Scanner sc;
    HashMap<Integer,Vertex> vertices = new HashMap<>();

    private int deadline;

    Graph(File file) {

        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    void constructGraph(){

        try {
            readNumOfVertices();

            while(sc.hasNextLine()){


                if (sc.hasNext(edgeEncoding)){

                    readEdge();

                }
                else if (sc.hasNext(vertexEncoding)){

                    readVertex();

                }
                else if (sc.hasNext(deadlineEncoding)){
                    readDeadline();
                }
                else{

                    throw new InputMismatchException("Unknown input parameter");

                }
                sc.nextLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sc.close();

    }

    private void readDeadline() {
        sc.skip(deadlineEncoding);
        deadline = sc.nextInt();
    }

    private void readVertex() {
        sc.skip(vertexEncoding);
        int vertexNum = sc.nextInt();
        if (sc.hasNext("P")){
            sc.skip(ignoreNonDigitsRegex);
            int persons = sc.nextInt();
            vertices.get(vertexNum).setPersons(persons);

        } else if (sc.hasNext("S")){
            vertices.get(vertexNum).setShelter();
        }
    }

    private void readEdge() {
        sc.skip(edgeEncoding);
        int in = sc.nextInt();
        int out = sc.nextInt();
        sc.skip(ignoreNonDigitsRegex);
        int weight = sc.nextInt();

        new Edge(vertices.get(in), vertices.get(out),weight);
    }

    private void readNumOfVertices() throws IOException{

        if (sc.hasNext(vertexEncoding)){

            sc.skip(vertexEncoding);
            int numOfVertices = sc.nextInt();
            for (int i = 1; i <= numOfVertices; i++) {
                vertices.put(i,new Vertex(i));
            }

            sc.nextLine();

        }
        else{
            throw new InputMismatchException("Missing number of vertices");
        }
    }

    void displayGraphState(){

        for (Vertex v: vertices.values()) {

            System.out.printf("Vertex No. %d: %d people",
                    v.getId(), v.getPersons());
            if (v.isShelter()){
                System.out.print(" ; has shelter.");
            }
            System.out.print("\nNeighbours: {");

            Iterator<Integer> neighboursIterator = v.getNeighbours().iterator();

            while (neighboursIterator.hasNext()){

                int neighbourId = neighboursIterator.next();

                System.out.printf("%d", neighbourId);

                if (v.getEdges().get(neighbourId).isBlocked()) {
                    System.out.print(" - blocked");
                }
                if (neighboursIterator.hasNext())
                    System.out.print(", ");
            }

            System.out.println("}\n");

        }
    }

    int getNumberOfVertices(){

        return vertices.size();

    }

    Vertex getVertex(int index){
        return vertices.get(index);
    }

    public int getDeadline() {
        return deadline;
    }

}
