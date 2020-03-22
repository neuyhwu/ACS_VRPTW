package VRPTW;

import java.util.ArrayList;
import java.util.Random;

/**  
* <p>Title: ACS</p>  
* <p>Description: </p>  
* @author zll_hust  
* @date 2020��2��2��  
*/
public class AntColonySystem {
	
	public double[][] Graph;
	public Customer[] customers;
	public ArrayList<Integer> untreated[]; // ��¼ÿһλagent kδ������Ŀͻ�
	public int customerNr; // �ͻ�����
	public int agentNr; // agent����
	public int capacity; // ��������
	public int IterMax; // ����������
	public Solution[] solutions; // agents
	public Solution bestSolution;
	public int[] r; // agent k ����λ�á���ǰλ�á���һλ��
	public double[][] pheromone; // ��Ϣ��
	public double[][] herustic; // ����ֵ
	public double[][] infoPhe; // infoPhe = pheromone ^ beta * herustic ^ sita
	public double pheromone_0; // ��Ϣ�س�ʼֵ
	public double w1, w2; // ������ʵĲ���
	public double alpha, beta, sita; // ����infoPhe�Ĳ�����
	public Random rand;
	
	public AntColonySystem(Parameter parameter, ReadIn readIn) {
		this.customerNr = readIn.customerNr;
		this.agentNr = customerNr;
		this.capacity = readIn.capacity;
		this.Graph = readIn.Graph;
		this.customers = readIn.customers;
		this.IterMax = parameter.IterMax;
		this.solutions = new Solution[agentNr + 10]; // ����agents�����ͳ�����һ����
		this.untreated = new ArrayList[agentNr + 10]; // ������������agents��
		for (int i = 0; i < agentNr + 10; i++) untreated[i] = new ArrayList<>();
		this.r = new int[agentNr + 10];
		this.pheromone = new double[customerNr + 10][customerNr + 10];
		this.herustic = new double[customerNr + 10][customerNr + 10];
		this.infoPhe = new double[customerNr + 10][customerNr + 10];
		this.alpha = parameter.Alpha;
		this.beta = parameter.Beta;
		this.sita = parameter.Sita;
		this.w1 = parameter.w1;
		this.w2 = parameter.w2;
		this.rand = new Random();
	}
	
	// ��ʼ���������
	public void init() {
		// ������Ϣ�س�ʼֵ
		double totalDistance = 0;
		double num = 0;
		for (int i = 0; i < customerNr + 1; i++) {
			for (int j = 0; j < customerNr + 1; j++) {
				if (i != j) {
					totalDistance += Graph[i][j];
					num ++;
				}
			}
		}
		pheromone_0 = num / (totalDistance * (customerNr + 1));
		
		// ��ʼ����Ϣ�ء�����ֵ
		for (int i = 0; i < customerNr + 1; i++) {
			for (int j = 0; j < customerNr + 1; j++) {
				if (i != j) {
					pheromone[i][j] = pheromone[j][i] = pheromone_0;
					herustic[i][j] = herustic[j][i] = 1 / Graph[i][j]; 
				}
			}
		}
	}
	
	// ��ʼ��agent����
	public void reset() {
		// ��ʼ��ÿλagentδ����Ŀͻ�
		for (int i = 0; i < agentNr; i++) {
			untreated[i].clear();
			for ( int j = 0; j < customerNr; j++) {
				untreated[i].add(j + 1);
			}
		}
		// ��ʼ����ʼ����ͻ�
		for (int i = 0; i < agentNr; i++) {
			solutions[i] = new Solution();
			r[i] = 0;
		}
		
	}
	
	// ����������
	public void construct_solution() {
		// Ϊÿһλagent�ֱ����
		for (int i = 0; i < agentNr; i++) {
			// ·����ʼ
			Route route = new Route();
			route.customers.add(0);
			
			while(untreated[i].size() != 0) {
				int next = select_next(i, route);
				
				// �����һ��ѡ�񲻺Ϸ���ͻ����������
				if (next == 0) {
					route.customers.add(0);
					route.time += Graph[r[i]][0];
					route.distance += Graph[r[i]][0];
					solutions[i].routes.add(route);
					solutions[i].totalCost += route.distance;
					route = new Route();
					route.customers.add(0);
					r[i] = 0;
				}
				else {
					route.customers.add(next);
					route.load += customers[next].Demand;
					route.time = Math.max(route.time + Graph[r[i]][next], customers[next].Begin) + customers[next].Service;
					route.distance += Graph[r[i]][next];
					r[i] = next;
					for (int j = 0; j < untreated[i].size(); j++) 
						if (untreated[i].get(j) == next) untreated[i].remove(j);
				}
			}
			// ���һ��·��������������
			route.customers.add(0);
			route.time = Math.max(Graph[r[i]][0], customers[0].Begin) + customers[0].Service;
			route.distance += Graph[r[i]][0];
			solutions[i].routes.add(route);
			solutions[i].totalCost += route.distance;
		}
	}
	
	public int select_next(int k, Route route) {
		// ��ȫ�������꣬������������
		if (untreated[k].size() == 0) return 0;
		
		// �������
		double sumPhe = 0;
		double sumTime = 0;
		double[] infoPhe = new double[agentNr];
		double[] infoTime = new double[agentNr];
		for (int i = 0; i < untreated[k].size(); i++) {
			infoPhe[i] =Math.pow(pheromone[r[k]][untreated[k].get(i)], beta) 
					* Math.pow(herustic[r[k]][untreated[k].get(i)], sita);
			infoTime[i] = 1 / (Math.abs(route.time - customers[untreated[k].get(i)].Begin) + 
					Math.abs(route.time - customers[untreated[k].get(i)].End));
			sumPhe += infoPhe[i];
			sumTime += infoTime[i];
		}
		
		double rate = rand.nextDouble();
		int next = 0;
		double sum_prob = 0;
		
		// ����0-1��������ۼӸ��ʣ������ڵ�ǰ�ۼӲ��֣����ص�ǰ���б��
		for (int i = 0; i < untreated[k].size(); i++) {
			sum_prob += infoPhe[i] * w1 / sumPhe + infoTime[i] * w2 / sumTime;
			if (rate < sum_prob) {
				next = untreated[k].get(i);
				// ����Ϸ���
				double time = route.time + Graph[r[k]][next];
				double load = route.load + customers[next].Demand;
				if (time > customers[next].End || load > capacity) 
					continue;
				else
					break;
			}
		}
		// ����Ϸ���
		double time = route.time + Graph[r[k]][next];
		double load = route.load + customers[next].Demand;
		if (time > customers[next].End || load > capacity) next = 0;
		
		return next;
	}
	
	// ������Ϣ��
	public void update_pheromone() {
		Solution now_best = new Solution();
		now_best.totalCost = Integer.MAX_VALUE;
		double delta = 0;
		
		// �������Ž�
		for (int i = 0; i < agentNr; i++) {
			if (solutions[i].totalCost < now_best.totalCost) now_best = solutions[i];
		}
		
		// �������Ž� ����ǰ���Ŵ�����ʷ���ţ�������Ϣ��ʱ�������
		if (now_best.totalCost < bestSolution.totalCost) {
			delta = (bestSolution.totalCost - now_best.totalCost) / bestSolution.totalCost;
			bestSolution = now_best;
		}
		
		//������Ϣ�غ��� 
		// ��Ϣ�ػӷ� 
		for (int i = 0; i < customerNr; i ++)
			for (int j = 0; j < customerNr; j ++)
				pheromone[i][j] *= (1 - alpha);
		// ��Ϣ������
		for (int i = 0; i < now_best.routes.size(); i ++){
			for (int j = 1; j < now_best.routes.get(i).customers.size(); j++) {
				pheromone[now_best.routes.get(i).customers.get(j - 1)][now_best.routes.get(i).customers.get(j)] 
						+= (1 / (double)now_best.totalCost) * (1 + delta);
				// �Գƴ���
				pheromone[now_best.routes.get(i).customers.get(j)][now_best.routes.get(i).customers.get(j - 1)] 
						= pheromone[now_best.routes.get(i).customers.get(j - 1)][now_best.routes.get(i).customers.get(j)];
			}
		}
	}
	
	public Solution ACS_Strategy() {
		bestSolution = new Solution();
		bestSolution.totalCost = Integer.MAX_VALUE;
		init();
		for (int i = 0; i < IterMax; i++) {
			reset();//��ʼ��agent��Ϣ 
			construct_solution();//�������е�agent����һ��������tour 
			update_pheromone();//������Ϣ�� 
			System.out.println("iteration : " + i + "\tbest solution cost = " + bestSolution.totalCost);
		}
		return bestSolution;
	}
}
