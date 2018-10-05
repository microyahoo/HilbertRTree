package com.njupt.cluster;

import java.util.*;

public class Kmeans {
	public ArrayList<General> allGenerals = null;
	public int totalNumber = 0;// 得到所有的武将数目
	public int K = 0;// 假设K=10

	public Kmeans() {
		allGenerals = new DomParser().prepare();
		totalNumber = allGenerals.size();
		K = 3;
	}

	// 第一次随机选取聚类中心
	public Set<Integer> firstRandom() {
		Set<Integer> center = new HashSet<Integer>();// 聚类中心的点的id,采用set保证不会有重复id
		Random ran = new Random();
		int roll = ran.nextInt(totalNumber);
		while (center.size() < K) {
			roll = ran.nextInt(totalNumber);
			center.add(roll);
		}
		return center;
	}

	// 根据聚类中心初始化聚类信息
	public ArrayList<Cluster> init(Set<Integer> center) {
		ArrayList<Cluster> cluster = new ArrayList<Cluster>();// 聚类 的数组
		Iterator<Integer> it = center.iterator();
		while (it.hasNext()) {
			Cluster c = new Cluster();// 代表一个聚类
			c.setCenter(it.next());
			cluster.add(c);
		}
		return cluster;
	}

	/**
	 * 计算各个武将到各个聚类中心的距离，重新聚类
	 * 
	 * @param cluster
	 *            聚类数组,用来聚类的，根据最近原则把武将聚类
	 * @param center
	 *            中心点id,用于计算各个武将到中心点的距离 return cluster 聚类后的所有聚类组成的数组
	 */
	public ArrayList<Cluster> cluster(Set<Integer> center,
			ArrayList<Cluster> cluster) {
		ArrayList<Distance> distence = new ArrayList<Distance>();// 存放距离信息,表示每个点到各个中心点的距离组成的数组
		General source = null;
		General dest = null;
		int id = 0;// 目的节点id
		int id2 = 0;// 源节点id
		Object[] p = center.toArray();// p 为聚类中心点id数组
		boolean flag = false;
		// 分别计算各个点到各个中心点的距离，并将距离最小的加入到各个聚类中，进行聚类
		for (int i = 0; i < totalNumber; i++) {
			// 每个点计算完,并聚类到距离最小的聚类中就清空距离数组
			distence.clear();
			// 计算到j个类中心点的距离,便利各个中心点
			for (int j = 0; j < center.size(); j++) {
				// 如果该点不在中心点内 则计算距离
				if (!(center.contains(i))) {
					flag = true;
					// 计算距离
					source = allGenerals.get(i);// 某个点
					dest = allGenerals.get((Integer) p[j]);// 各个 中心点
					// 计算距离并存入数组
					distence.add(new Distance((Integer) p[j], i, Tool.juli(
							source, dest)));
				} else {
					flag = false;
				}
			}
			// 说明计算完某个武将到类中心的距离,开始比较
			if (flag == true) {
				// 排序比较一个点到各个中心的距离的大小,找到距离最小的武将的 目的id,和源id,
				// 目的id即类中心点id，这个就归到这个中心点所在聚类中
				double min = distence.get(0).getDist();// 默认第一个distance距离是最小的
				// 从1开始遍历distance数组
				int minid = 0;
				for (int k = 1; k < distence.size(); k++) {
					if (min > distence.get(k).getDist()) {
						min = distence.get(k).getDist();
						id = distence.get(k).getDest();// 目的，即类中心点
						id2 = distence.get(k).getSource();// 某个武将
						minid = k;
					} else {
						id = distence.get(minid).getDest();
						id2 = distence.get(minid).getSource();
					}
				}
				// 遍历cluster聚类数组，找到类中心点id与最小距离目的武将id相同的聚类
				for (int n = 0; n < cluster.size(); n++) {
					// 如果和中心点的id相同 则setError
					if (cluster.get(n).getCenter() == id) {
						cluster.get(n).addGeneral(allGenerals.get(id2));// 将与该聚类中心距离最小的武将加入该聚类
						break;
					}
				}
			}
		}
		return cluster;
	}

	// 产生新的聚类中心点数组
	public Set<Integer> updateCenter() {
		Set<Integer> center = new HashSet<Integer>();
		for (int i = 0; i < K; i++) {
			center.add(i);
		}
		return center;
	}

	// 更新聚类中心, 求平均值
	public ArrayList<Cluster> updateCluster(ArrayList<Cluster> cluster) {
		ArrayList<Cluster> result = new ArrayList<Cluster>();
		// 重新产生的新的聚类中心组成的数组
		// k个聚类进行更新聚类中心
		for (int j = 0; j < K; j++) {
			ArrayList<General> ps = cluster.get(j).getOfCluster();// 该聚类的所有 武将
																	// 组成的数组
			ps.add(allGenerals.get(cluster.get(j).getCenter()));// 同时将该类中心对应的武将加入该武将数组
			int size = ps.size();// 该聚类的长度大小
			// 计算和，然后在计算平均值
			int sumrender = 0, sumtongshai = 0, sumwuli = 0, sumzhili = 0, sumjibin = 0, sumnubin = 0, sumqibin = 0, sumpolic = 0, sumqiangbin = 0, sumbinqi = 0, sumtongwu = 0, sumtongzhi = 0, sumtongwuzhi = 0, sumtongwuzhizheng = 0, sumsalary = 0;
			for (int k1 = 0; k1 < size; k1++) {
				sumrender += ps.get(k1).getRender();
				sumtongshai += ps.get(k1).getRender();
				sumwuli += ps.get(k1).getWuli();
				sumzhili += ps.get(k1).getZhili();
				sumjibin += ps.get(k1).getJibin();
				sumnubin += ps.get(k1).getNubin();
				sumqibin += ps.get(k1).getQibin();
				sumpolic += ps.get(k1).getPolic();
				sumqiangbin += ps.get(k1).getQiangbin();
				sumbinqi += ps.get(k1).getBinqi();
				sumtongwu += ps.get(k1).getTongwu();
				sumtongzhi += ps.get(k1).getTongzhi();
				sumtongwuzhi += ps.get(k1).getTongwuzhi();
				sumtongwuzhizheng += ps.get(k1).getTongwuzhizheng();
				sumsalary += ps.get(k1).getSalary();
			}
			// 产生新的聚类，然后加入到聚类数组中
			Cluster newCluster = new Cluster();
			newCluster.setCenter(j);
			// 计算平均值并构造新的武将对象
			newCluster.addGeneral(new General(sumrender / size, sumtongshai
					/ size, sumwuli / size, sumzhili / size, sumjibin / size,
					sumnubin / size, sumqibin / size, sumpolic = 0,
					sumqiangbin = 0, sumbinqi / size, sumtongwu / size,
					sumtongzhi / size, sumtongwuzhi / size, sumtongwuzhizheng
							/ size, sumsalary / size));
			result.add(newCluster);
		}
		return result;

	}

	/**
	 * 计算各个武将到各个更新后的聚类中心的距离，重新聚类
	 * @param update 更新后的聚类中心
	 * @param cluster 要存储的聚类中心
	 */
	public ArrayList<Cluster> updateJuLei(ArrayList<Cluster> update,
			ArrayList<Cluster> cluster) {
		ArrayList<Distance> distence = new ArrayList<Distance>();// 存放距离信息,表示每个点到各个中心点的距离组成的数组
		General source = null;
		General dest = null;
		int id = 0;// 目的节点id
		int id2 = 0;// 源节点id
		//Object[] p = center.toArray();// p 为聚类中心点id数组
		boolean flag = false;
		// 分别计算各个点到各个中心点的距离，并将距离最小的加入到各个聚类中，进行聚类
		for (int i = 0; i < totalNumber; i++) {
			// 每个点计算完,并聚类到距离最小的聚类中就清空距离数组
			distence.clear();
			// 计算到j个类中心点的距离,便利各个中心点
			//for (int j = 0; j < center.size(); j++) {
			for (int j = 0; j < update.size(); j++) {
				// 如果该点不在中心点内 则计算距离
				//if (!(center.contains(i))) {
					flag = true;
					// 计算距离
					source = allGenerals.get(i);// 某个点
					// dest = allGenerals.get((Integer) p[j]);// 各个 中心点
					dest = update.get(j).getOfCluster().get(0);// 各个 中心点
					// 计算距离并存入数组
					//distence.add(new Distance((Integer) p[j], i, Tool.juli(
					distence.add(new Distance(update.get(j).getCenter(), i, Tool.juli(
							source, dest)));
					/*} else {
					flag = false;
				}*/
			}
			// 说明计算完某个武将到类中心的距离,开始比较
			if (flag == true) {
				// 排序比较一个点到各个中心的距离的大小,找到距离最小的武将的 目的id,和源id,
				// 目的id即类中心点id，这个就归到这个中心点所在聚类中
				double min = distence.get(0).getDist();// 默认第一个distance距离是最小的
				// 从1开始遍历distance数组
				int mid = 0;
				for (int k = 1; k < distence.size(); k++) {
					if (min > distence.get(k).getDist()) {
						min = distence.get(k).getDist();
						id = distence.get(k).getDest();// 目的，即类中心点
						id2 = distence.get(k).getSource();// 某个武将
						mid = k;
					} else {
						id = distence.get(mid).getDest();
						id2 = distence.get(mid).getSource();
					}
				}
				// 遍历cluster聚类数组，找到类中心点id与最小距离目的武将id相同的聚类
				for (int n = 0; n < cluster.size(); n++) {
					// 如果和中心点的id相同 则setError
					if (cluster.get(n).getCenter() == id) {
						cluster.get(n).addGeneral(allGenerals.get(id2));// 将与该聚类中心距离最小的武将加入该聚类
					}
				}
			}
		}
		return cluster;
	}

	// 不断循环聚类直到各个聚类没有重新分配
	public ArrayList<Cluster> getResult() {
		ArrayList<Cluster> result = new ArrayList<Cluster>();
		ArrayList<Cluster> temp = new ArrayList<Cluster>();
		boolean flag = false;
		// 得到随机中心点然后进行聚类
		Set<Integer> center = firstRandom();
		result = cluster(center, init(center));
		print(result);
		do {
			// 重新聚类
			ArrayList<Cluster> up = updateCluster(result);//新的聚类中心
			ArrayList<Cluster> cluster = init(updateCenter()); // 得到更新后的中心点对应的聚类数组
			temp = updateJuLei(up, cluster);
			//print(temp);
			flag = isEquals(temp, result);
			result = temp;
		} while (!flag);
		return result;
	}
	
	public boolean isEquals(ArrayList<Cluster> temp, ArrayList<Cluster> result){
		boolean flag = false;
		if(temp.size() != result.size()){
			return flag;
		}
		for(Cluster tem : temp){
			for(Cluster res : result){
				if(tem.getCenter() == res.getCenter()){
					flag = true;
				}
			}
			// 如果找了一轮没找到 则说明两个聚类
			if(flag == false){
				return false;
			}else{// 如果找了一轮找到了，那么接着找
				flag = false;
			}
		}
		//如果代码能进行到这边，说明是true
		flag = true;
		return flag;
	}
	
	//输出所有的聚类
	public void print(ArrayList<Cluster> cs) {
		System.out.println("***************************************");
		for (int i = 0; i < cs.size(); i++) {
			Cluster c = cs.get(i);
			System.out.println("-----------------------------------------------------");
			System.out.println("center: " + allGenerals.get(c.getCenter()));
			ArrayList<General> p = c.getOfCluster();
			for (int j = 0; j < p.size(); j++) {
				System.out.println("general:"+p.get(j)+"\n");
			}
		}
	}

}
