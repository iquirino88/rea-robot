package com.rearobot.robot.service.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rearobot.board.model.Board;
import com.rearobot.robot.json.RobotPlayActionJson;
import com.rearobot.robot.model.Robot;
import com.rearobot.robot.model.enuns.Action;
import com.rearobot.robot.model.enuns.Direction;
import com.rearobot.robot.service.RobotService;
import com.rearobot.robot.service.validator.RobotServiceValidator;

/**
 * @author iago
 */
@Service
public class RobotServiceImpl implements RobotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RobotServiceImpl.class);

    @Autowired
    private RobotServiceValidator validator;

    @Value("${board.width:5}")
    private Integer boardWidth;

    @Value("${board.height:5}")
    private Integer boardHeight;

    /*
     * (non-Javadoc)
     *
     * @see com.rearobot.robot.service.RobotService#play (com.rearobot.robot.json.RobotPlayActionJson)
     */
    @Override
    public Robot play(RobotPlayActionJson playAction) {
        return this.play(playAction.getPositionX(), playAction.getPositionY(), playAction.getDirection(),
                playAction.getActions());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.rearobot.robot.service.RobotService#play(java.lang.Integer, java.lang.Integer,
     * com.rearobot.robot.model.enuns.Direction, java.util.List)
     */
    @Override
    public Robot play(Integer positionX, Integer positionY, Direction direction,
            List<Action> actions) {
        LOGGER.info("Start playing with robot.");
        validator.validate(positionX, positionY, direction);
        Board board = new Board(boardWidth, boardHeight);
        validateWhenSetRobot(positionX, positionY, board);
        Robot robot = new Robot(positionX, positionY, direction, actions);
        robot.place();
        performActions(board, robot);
        LOGGER.info("Finished playing with robot.");
        return robot;
    }

    /**
     * Validate When Place robot on board.
     *
     * @param positionX
     * @param positionY
     * @param board
     */
    private void validateWhenSetRobot(Integer positionX, Integer positionY, Board board) {
        if (!board.isValidPosition(positionX, positionY)) {
            throw new IllegalArgumentException("Invalid position to place your robot it will falls :(");
        }
    }

    /**
     * Peform Robot Actions.
     *
     * @param board
     * @param robot
     */
    private void performActions(Board board, Robot robot) {
        if (CollectionUtils.isNotEmpty(robot.getProgrammigActions())) {
            robot.getProgrammigActions().forEach(action -> {
                performAction(board, robot, action);
            });
        }
    }

    /**
     * Perform Action.
     *
     * @param board
     * @param robot
     * @param action
     */
    private void performAction(Board board, Robot robot, Action action) {
        if (Action.MOVE.equals(action)) {
            if (robot.isAbleToMove(board)) {
                robot.move();
            } else {
                LOGGER.info("The next move will bring down the robot on the floor.");
            }
        } else if (Action.LEFT.equals(action)) {
            robot.turnLeft();
        } else if (Action.RIGHT.equals(action)) {
            robot.turnRight();
        } else if (Action.REPORT.equals(action)) {
            robot.report();
            LOGGER.info("=== Report Status === Position X: {}, Position Y: {}, Current Direction: {}.",
                    robot.getPositionX(),
                    robot.getPositionY(),
                    robot.getDirection());
        } else {
            throw new IllegalArgumentException("Invalid action your robot can`t do that.");
        }
    }

}
