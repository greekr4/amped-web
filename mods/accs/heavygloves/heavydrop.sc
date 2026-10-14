0 : {
		bind(1000);
		stop();
		changeFace(2);
		fadeinAnimation( 335.0 , 335.0 , 335.0 , 4.5 , 0, 0.2) ;
	}
200 : {
		move(0,-6,0.5);
		startAnimation( 335.0 , 336.0 , 336.0 , 10.0 , 0) ;
	}
450 : {
		makeShapeModel("/effect/quake.oa",0,-0.3,0,0,1,0);
		makeShapeModel("/effect/shock.oa",0,-0.2,0,0,1,0);
		makeShapeModel("/effect/crash.oa",0,-0.1,0,0,1,0);
		attack("drop0");
		attack("drop90");
		attack("drop180");
		attack("drop270");
	}
550 : {
		move(0,0,0);
		stop();
		startAnimation( 336.0 , 337.0 , 337.0 , 6.0 , 0) ;
	}
700 : {
		startAnimation( 338.0 , 339.0 , 339.0 , 4.0 , 0) ;
	}
1000 : {
		recover();
	}
