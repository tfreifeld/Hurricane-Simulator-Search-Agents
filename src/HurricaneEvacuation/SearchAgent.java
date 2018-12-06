package HurricaneEvacuation;

import java.util.HashMap;
import java.util.Map;

abstract class SearchAgent extends Agent {

    private Node node;

    SearchAgent(int agentNum) {

        this.agentNum = agentNum;
        this.node = null;
    }

    abstract HeuristicSearch getSearch(Vertex location);

    @Override
    Move makeOperation() {

        if (this.node == null){

            State currentState = new State(Simulator.getInitialPeopleMap(), location, Simulator.getTotalPeople());

            this.node = getSearch(this.getLocation()).run();

            if (this.node == null){
                /*If no path has been found -> NoOp*/
                return new Move(this, this.getLocation(),null);
            }

            while (!(this.node.getParent().getState().equals(currentState))){
                this.node.getParent().setChosenChild(this.node);
                this.node = this.node.getParent();
            }
        }

        Edge edge;

        try {
            edge = this.getLocation()
                    .getNeighbour(this.node.getState().getLocation().getId());
        } catch (Vertex.NotNeighbourException e) {
            e.printStackTrace();
            this.node = null;
            return new Move(this, this.getLocation(), null);
        }
        Move move = new Move(this, this.node.getState().getLocation(), edge);

        this.node = this.node.getChosenChild();

        return move;
    }

    static abstract class HeuristicSearch extends Search{

        HeuristicSearch() {
            super(node -> node.getPathCost() > Simulator.getDeadline());
        }
    }

    static abstract class HeuristicNode extends Node{

        private int carrying;
        private double heuristicValue;

        HeuristicNode(Vertex location) {
            super();

            this.carrying = 0;

            this.state = new State(Simulator.getInitialPeopleMap(), location, Simulator.getTotalPeople());
            this.heuristicValue = computeEvaluationFunction();
        }

        HeuristicNode(Vertex location, HeuristicNode parent, Edge edge) {
            super(parent);

            this.pathCost = parent.getPathCost()
                    + edge.getWeight() * (1 + Simulator.getKFactor() /*TODO: need to multiply in vehicle load*/);

            this.carrying = parent.getCarrying();

            HashMap<Integer,Integer> tempPeopleMap = new HashMap<>(parent.getState().getPeopleMap());
            int tempLeftToSave = parent.getState().getLeftToSave();

            if(location.getPersons() > 0){
                this.carrying += location.getPersons();
                tempPeopleMap.replace(location.getId(), 0);
            }

            if(location.isShelter()){
                tempLeftToSave -= this.carrying;
                this.carrying = 0;
            }

            this.state = new State(tempPeopleMap,location, tempLeftToSave);
            this.heuristicValue = computeEvaluationFunction();
        }

        int getCarrying() {
            return carrying;
        }

        double computeEvaluationFunction() {

            int result = 0;

            /*TODO: consider adding a condition if this a goal test then return zero*/

            HashMap<Integer,Double> lengthsToPeople = getState().getLocation().getLengthsToPeople();
            for (Map.Entry<Integer,Integer> entry: getState().getPeopleMap().entrySet()){
                if (entry.getValue() > 0){
                    double lengthToShelter =
                            Simulator.getGraph().getVertex(entry.getKey()).getLengthToClosestShelter();
                    if (getPathCost() + lengthsToPeople.get(entry.getKey())
                            + lengthToShelter > Simulator.getDeadline()){
                        result += entry.getValue() * 100;
                    }
                }
            }

            return result;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.heuristicValue, ((HeuristicNode)o).heuristicValue);

        }
    }
}