package model.incident;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.log.Activity;
import evaluation.CostModel;
import evaluation.QueryEngine;

public class ConsOperator extends Operator {
	public Map<Long, List<Occurrence>> execute(Map<Long, List<Occurrence>> occs1, Map<Long, List<Occurrence>> occs2){
		Map<Long, List<Occurrence>> res = new HashMap<Long, List<Occurrence>>();
		if(occs1.size() == 0 || occs2.size() == 0){
			return res;
		}
		
		for(long key: occs1.keySet()){
			if(!occs2.containsKey(key))
				continue;
			List<Occurrence> li1 = occs1.get(key);
			List<Occurrence> li2 = occs2.get(key);
			for(Occurrence occ1: li1){
				for(Occurrence occ2: li2){
					if(occ1.end+1 == occ2.start){
						if(!res.containsKey(key)){
							res.put(key, new ArrayList<Occurrence>());
						}
						res.get(key).add(merge(occ1, occ2));
					}
				}
			}
		}
		return res;
	}

	private Occurrence merge(Occurrence occ1, Occurrence occ2) {
		Occurrence occ = new Occurrence(occ1.wid);
		occ.seq.addAll(occ1.seq);
		occ.seq.addAll(occ2.seq);
		occ.setTimeInterval(occ1.start, occ2.end);
		
		//Merge the effects on attributes
//		occ.preMap.putAll(occ1.preMap);
//		occ.postMap.putAll(occ1.postMap);
//		occ.postMap.putAll(occ2.postMap);
		occ.setPreMap(occ1.preMap);
		occ.setPostMap(occ2.postMap);
		return occ;
	}

	@Override
	public long getResultSize1(long c1, long c2) {
		return Math.min(c1, c2);
	}

	@Override
	public CostModel getResultSize2(CostModel a1, CostModel a2) {
		if(a1.count == 0 || a2.count == 0 || a1.numStart == 0 || a2.numStart == 0){
			return new CostModel(a1.name);
		}
		
		long ts1 = a1.aveStart, td1 = a1.aveInterval;
		long ts2 = a2.aveStart, td2 = a2.aveInterval;
		CostModel cur = new CostModel(a1.name);
		cur.aveStart = Math.max(a1.aveStart, a2.aveStart) - 1;
		cur.aveInterval = Math.max(a1.aveInterval*a2.aveInterval/gcd(a1.aveInterval, a2.aveInterval), 1);
		long end = Math.min(ts1+td1*a1.count/a1.numStart, ts2+td2*a2.count/a1.numStart);
		cur.count = (end-cur.aveStart+1)/cur.aveInterval;
		cur.numStart = Math.min(a1.numStart, cur.numStart);
		return cur;
	}

	private long gcd(long a, long b) {
		if(b == 0) return a;
		return gcd(b, a%b);
	}
}
