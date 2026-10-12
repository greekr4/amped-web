0 : {
		stop();
		changeFace(1);
		fadeinAnimation( 235.0 , 235.0 , 235.0 , 4.5 , 0, 0.2) ;
	}
150 : {
		makeParticle("fcharge.ptc",0.2,-0.1,-0.35,0,1,0);
	}
520 : {
		move(0,2);
		startAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0) ;
		attack("bakupunch");
	}
600 : {
		move(0,3);
	}
650 : {
		makeShapeModel("/effect/crash.oa",0,0,0.75,0,0,1,1);
		makeShapeModel("/effect/explode2.oa",0,0.1,0.9,0,0,1);
	}
700 : {
		attack("bakuwave");
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
1000 : {
		stop();
		nextTech();
	}
1300 : {
		recover();
	}
