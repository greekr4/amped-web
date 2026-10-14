0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 380.0 , 380.0 , 380.0 , 4.5 , 0, 0.22) ;
		ignoreAttack(1500);
		bind(1500);
		makeShapeModel("/effect/elec.oa",0,0.3,0.3,0,0,1);
	}
250 : {
		move(0,2.6);
		makeShapeModel("/effect/spark.oa",0,0.1,0.5,0,0,1,1);
		startAnimation( 381.0 , 383.0 , 381.0 , 24.0 , 1) ;
	}
350 : {
		attack("rush1");
	}
500 : {
		attack("rush2");
		makeShapeModel("/effect/spark.oa",0.3,0.2,0.5,0,0,1,1);
	}
650 : {
		attack("rush3");
		makeShapeModel("/effect/spark.oa",-0.3,0.2,0.5,0,0,1,1);
	}
950 : {
		stop();
		makeShapeModel("/effect/flash.oa",0,0.3,0,0,1,0);
		fadeinAnimation( 384.0 , 384.0 , 384.0 , 3.0 , 0, 0.3) ;
	}
1200 : {
		startAnimation( 384.0 , 385.0 , 385.0 , 5.0 , 0) ;
	}
1500 : {
		recover();
	}
