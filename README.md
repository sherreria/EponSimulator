EponSimulator
=============

Java simulator for upstream EPON (Ethernet Passive Optical Network) channels. 

Overview
--------

This java program simulates an upstream EPON channel. The upstream EPON channel is shared among all ONUs using time division multiple access (TDMA). The OLT allocates the appropriate share of upstream bandwidth to each ONU with the help of a dynamic bandwidth allocation (DBA) algorithm. Additionally, to reduce power consumption, ONUs can enter a low power state, known as doze mode, that makes their transmitter sleep when there is no upstream traffic.

Invocation
----------

`java EponSimulator [-n num_onus] [-l simulation_length (s)] [-s simulation_seed] [-t traffic_rate (b/s)] [-p packet_size (bytes)] [-g traffic_distribution (deterministic|poisson|pareto)] [-q queue_threshold (packets)] [-m maximum_queue_size (packets)] [-c uplink_capacity (b/s)] [-d dba_cycle (s)] [-a dba_algorithm (fixed|fair|proportional|gated|limited|limitedExcess)] [-w onu_wakeup (s)] [-r onu_refresh_timeout (s)] [-e onu_doze_mode_energy_ratio] [-f traffic_profiles] [-u] [-v]`

Output
------

The simulator outputs a summary of each ONU statistics:

    - Number of packets received, sent and dropped

    - Average packet delay

    - Time in each possible state

    - Energy consumption

With option -v, the simulator outputs a line every time an important event happens:
    `time ONU onu_id event event_info`

Legal
-----

Copyright ⓒ Sergio Herrería Alonso <sha@det.uvigo.es> 2013

This simulator is licensed under the GNU General Public License, version 3 (GPL-3.0). For more information see LICENSE.txt
