-1: {
	attackNow(0,0,0,1.5,16,"hit_freeze.sc",-1,false);
	play("/sound/7.sr",100,1);
	checkMask(0);
	}
0 : {
	makeShapeModel("/effect/frzflush.oa",0,0.1,0,0,1,0);
	makeShapeModel("/effect/iceshot_clash.oa",0.8,0,0,1,0,0);
	makeShapeModel("/effect/iceshot_clash.oa",-0.8,0,0,-1,0,0);
	makeShapeModel("/effect/iceshot_clash.oa",0,0,0.8,0,0,1);
	makeShapeModel("/effect/iceshot_clash.oa",0,0,-0.8,0,0,-1);
	makeShapeModel("/effect/cold1.oa",0,0.3,0,0,1,0);
	invisible();
	}
800 : {
	kill();
	}
