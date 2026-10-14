-1: {
	attackNow(0,0,0,1.3,12,"hit_shock.sc",-1,false);
	play("/sound/3.sr",100,1);
	checkMask(0);
	}
0 : {
	makeShapeModel("/effect/spark.oa",0,0.2,0,0,1,0);
	makeShapeModel("/effect/elec.oa",0.5,0.1,0,0,1,0);
	makeShapeModel("/effect/elec.oa",-0.5,0.1,0,0,1,0);
	makeShapeModel("/effect/spark.oa",0,0.1,0,0,1,0);
	invisible();
	}
800 : {
	kill();
	}
