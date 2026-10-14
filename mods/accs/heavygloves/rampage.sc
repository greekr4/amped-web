0 : {
		bind(1800);
		stop();
		changeFace(2);
		fadeinAnimation( 380.0 , 380.0 , 380.0 , 4.5 , 0, 0.22) ;
		ignoreAttack(700);
	}
250 : {
		move(0,2.4);
		makeShapeModel("/effect/spark.oa",0,0.1,0.5,0,0,1,1);
		startAnimation( 381.0 , 383.0 , 381.0 , 24.0 , 1) ;
	}
350 : {
		attack("rampage");
	}
550 : {
		attack("rampage");
		makeShapeModel("/effect/spark.oa",0,0.1,0.5,0,0,1,1);
	}
750 : {
		attack("rampage");
	}
950 : {
		stop();
		changeFace(1);
		fadeinAnimation( 235.0 , 235.0 , 235.0 , 4.5 , 0, 0.15) ;
	}
1100 : {
		move(0,2.0);
		startAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0) ;
	}
1180 : {
		makeShapeModel("/effect/crash.oa",0,0.2,0.8,0,0,1);
		makeShapeModel("/effect/flash.oa",0.2,-0.1,-0.5,0,1,0);
		attack("rampageEnd");
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
1350 : {
		stop();
	}
1800 : {
		recover();
	}
