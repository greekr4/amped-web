0 : {
		stop();
		changeFace(1);
		fadeinAnimation( 220.0 , 220.0 , 220.0 , 4.5 , 0, 0.24) ;
	}
320 : {
		move(0,1.0);
		startAnimation( 220.0 , 221.0 , 221.0 , 8.0 , 0) ;
	}
420 : {
		stop();
		makeShapeModel("/effect/quake.oa",0,0.0,0.7,0,1,0);
		makeShapeModel("/effect/shock.oa",0,0.05,0.7,0,1,0);
		attack("smash0");
		attack("smashL");
		attack("smashR");
		startAnimation( 221.0 , 223.0 , 221.0 , 24.0 , 1) ;
	}
900 : {
		nextTech();
	}
1100 : {
		recover();
	}
