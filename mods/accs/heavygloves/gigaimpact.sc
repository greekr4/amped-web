0 : {
		bind(1600);
		stop();
		changeFace(2);
		fadeinAnimation( 250.0 , 250.0 , 250.0 , 4.5 , 0, 0.16) ;
		ignoreAttack(1100);
		makeShapeModel("/effect/lcharge.oa",0,0.25,0.3,0,0,1,1);
	}
500 : {
		makeShapeModel("/effect/lcharge.oa",0,0.25,0.3,0,0,1,1);
		fadeinAnimation( 235.0 , 235.0 , 235.0 , 4.5 , 0, 0.2) ;
	}
800 : {
		move(0,3.0);
		startAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0) ;
	}
900 : {
		makeShapeModel("/effect/crash.oa",0,0.2,0.8,0,0,1);
		makeShapeModel("/effect/shock.oa",0,0.2,0.8,0,0,1);
		makeShapeModel("/effect/flash.oa",0.2,-0.1,-0.5,0,1,0);
		attack("giga");
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
1100 : {
		stop();
	}
1600 : {
		recover();
	}
