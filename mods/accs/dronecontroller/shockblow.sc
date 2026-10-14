0 : {
		move(0,1.4);
		fadeinAnimation( 245.0 , 245.0 , 245.0 , 4.5 , 0, 0.2) ;
	}
220 : {
		stop();
		makeShapeModel("/effect/spark.oa",0.2,0.0,0.5,0,0,1,1);
		startAnimation( 245.0 , 246.0 , 246.0 , 6.0 , 0) ;
	}
340 : {
		attack("shockblow_rb");
	}
390 : {
		attack("shockblow");
		makeShapeModel("/effect/shock.oa",0,0.1,0.7,0,0,1);
		startAnimation( 246.0 , 248.0 , 246.0 , 24.0 , 1) ;
	}
580 : {
		nextTech();
	}
800 : {
		recover();
	}
