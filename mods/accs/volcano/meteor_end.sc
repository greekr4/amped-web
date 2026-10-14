-1: {
	attackNow(0,0,0,1.5,18,"hit_explode.sc",-1,false);
	play("/sound/5.sr",100,1);
	checkMask(0);
	}
0 : {
	makeShapeModel("/effect/explode2.oa",0,0,0,0,1,0);
	makeShapeModel("/effect/crash.oa",0,0.1,0,0,1,0);
	makeShapeModel("/effect/honoo.oa",0.6,0.1,0,0,1,0);
	makeShapeModel("/effect/honoo.oa",-0.6,0.1,0,0,1,0);
	invisible();
	}
800 : {
	kill();
	}
