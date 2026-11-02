-- =====================================================================
--  Student Registration System - Sample Data
--  Run AFTER schema.sql:  mysql -u root -p < database/sample_data.sql
--
--  All people below are FICTIONAL. Email addresses use example.com,
--  a domain reserved for documentation, and phone numbers are made up.
--
--  INSERT IGNORE skips rows whose roll number or email already exists,
--  so running this file twice does not create duplicates.
--  Registration dates are relative to "now" so the dashboard's
--  "Recent Registrations" list always looks realistic.
-- =====================================================================

USE student_registration_db;

INSERT IGNORE INTO students
    (roll_number, full_name, email, phone, gender, dob, department, course, semester, address, status, created_at)
VALUES
    ('23CSE001', 'Aarav Mehta',      'aarav.mehta@example.com',      '9000000001', 'Male',   '2005-03-14', 'Computer Science and Engineering',         'B.E.',   3, '12 Green Park Colony, Sector 5, Chandigarh',       'Active',   NOW() - INTERVAL 30 DAY),
    ('23CSE002', 'Diya Sharma',      'diya.sharma@example.com',      '9000000002', 'Female', '2005-07-22', 'Computer Science and Engineering',         'B.E.',   3, '45 Rose Avenue, Model Town, Ludhiana',             'Active',   NOW() - INTERVAL 28 DAY),
    ('22CSE014', 'Kabir Singh',      'kabir.singh@example.com',      '9000000003', 'Male',   '2004-11-02', 'Computer Science and Engineering',         'B.E.',   5, '7 Lake View Road, Phase 2, Mohali',                'Active',   NOW() - INTERVAL 26 DAY),
    ('24IT003',  'Ananya Iyer',      'ananya.iyer@example.com',      '9000000004', 'Female', '2006-01-30', 'Information Technology',                   'B.Tech', 1, '88 Temple Street, Mylapore, Chennai',              'Active',   NOW() - INTERVAL 24 DAY),
    ('23IT021',  'Rohan Verma',      'rohan.verma@example.com',      '9000000005', 'Male',   '2005-05-09', 'Information Technology',                   'B.Tech', 3, '19 Civil Lines, Near City Mall, Jaipur',           'Inactive', NOW() - INTERVAL 22 DAY),
    ('22ECE007', 'Ishita Banerjee',  'ishita.banerjee@example.com',  '9000000006', 'Female', '2004-08-17', 'Electronics and Communication Engineering','B.E.',   5, '3 Park Circus, Ballygunge, Kolkata',               'Active',   NOW() - INTERVAL 20 DAY),
    ('21ECE030', 'Vivaan Reddy',     'vivaan.reddy@example.com',     '9000000007', 'Male',   '2003-12-25', 'Electronics and Communication Engineering','B.E.',   7, '56 Jubilee Hills, Road No. 10, Hyderabad',         'Active',   NOW() - INTERVAL 18 DAY),
    ('23EE011',  'Meera Nair',       'meera.nair@example.com',       '9000000008', 'Female', '2005-02-11', 'Electrical Engineering',                   'B.E.',   3, '21 Marine Drive, Ernakulam, Kochi',                'Active',   NOW() - INTERVAL 16 DAY),
    ('22ME019',  'Arjun Patel',      'arjun.patel@example.com',      '9000000009', 'Male',   '2004-04-03', 'Mechanical Engineering',                   'B.E.',   5, '9 Satellite Road, Near Lake, Ahmedabad',           'Active',   NOW() - INTERVAL 14 DAY),
    ('21ME002',  'Sanya Kapoor',     'sanya.kapoor@example.com',     '9000000010', 'Female', '2003-09-19', 'Mechanical Engineering',                   'B.E.',   7, '14 Mall Road, Near Clock Tower, Shimla',           'Inactive', NOW() - INTERVAL 12 DAY),
    ('24CE005',  'Aditya Kulkarni',  'aditya.kulkarni@example.com',  '9000000011', 'Male',   '2006-06-28', 'Civil Engineering',                        'B.E.',   1, '33 FC Road, Shivajinagar, Pune',                   'Active',   NOW() - INTERVAL 10 DAY),
    ('24BCA012', 'Nisha Gupta',      'nisha.gupta@example.com',      '9000000012', 'Female', '2006-10-05', 'Computer Applications',                    'BCA',    1, '71 Hazratganj, Near Main Market, Lucknow',         'Active',   NOW() - INTERVAL 8 DAY),
    ('23BCA018', 'Yash Malhotra',    'yash.malhotra@example.com',    '9000000013', 'Male',   '2005-12-12', 'Computer Applications',                    'BCA',    3, '28 Rajouri Garden, Block C, New Delhi',            'Active',   NOW() - INTERVAL 6 DAY),
    ('25MCA004', 'Kavya Joshi',      'kavya.joshi@example.com',      '9000000014', 'Female', '2002-03-27', 'Computer Applications',                    'MCA',    1, '5 Rajpur Road, Near Clock Tower, Dehradun',        'Active',   NOW() - INTERVAL 4 DAY),
    ('25MTE001', 'Siddharth Rao',    'siddharth.rao@example.com',    '9000000015', 'Male',   '2001-07-15', 'Computer Science and Engineering',         'M.Tech', 1, '62 Indiranagar, 100 Feet Road, Bengaluru',         'Active',   NOW() - INTERVAL 2 DAY),
    ('24CSE040', 'Zara Khan',        'zara.khan@example.com',        '9000000016', 'Female', '2006-02-08', 'Computer Science and Engineering',         'B.E.',   1, '17 Banjara Hills, Road No. 3, Hyderabad',          'Active',   NOW() - INTERVAL 1 DAY);
